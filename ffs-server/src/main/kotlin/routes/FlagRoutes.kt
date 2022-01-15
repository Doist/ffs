@file:Suppress("LocalVariableName", "VariableNaming", "TooManyFunctions")

package doist.ffs.routes

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import doist.ffs.auth.Permission
import doist.ffs.auth.TokenPrincipal
import doist.ffs.db.Flag
import doist.ffs.db.capturingLastInsertId
import doist.ffs.db.flags
import doist.ffs.ext.authorizeForProject
import doist.ffs.ext.optionalRoute
import doist.ffs.ext.stream
import doist.ffs.plugins.database
import doist.ffs.rule.validateFormula
import doist.ffs.serialization.json
import doist.ffs.sse.SseEvent
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.plugins.MissingRequestParameterException
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.request.acceptItems
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.util.getOrFail
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonObject
import routes.PATH_LATEST
import kotlin.time.Duration.Companion.minutes

const val PATH_FLAGS = "/flags"
const val PATH_EVAL = "/eval"
const val PATH_ARCHIVE = "/archive"

@Suppress("FunctionName")
fun PATH_FLAG(id: Any) = "$PATH_FLAGS/$id"

fun Application.installFlagRoutes() = routing {
    optionalRoute(PATH_LATEST) {
        route("$PATH_PROJECTS/{id}/$PATH_FLAGS") {
            authenticate("session") {
                createFlag()
                getFlags()
            }
        }

        route(PATH_FLAGS) {
            authenticate("session") {
                getFlag()
                updateFlag()
            }

            authenticate("token") {
                getFlags()
                getFlagsEval()
            }

            route("/{id}/$PATH_ARCHIVE") {
                authenticate("session") {
                    archiveFlag()
                    unarchiveFlag()
                }
            }
        }
    }
}

/**
 * Create a new flag.
 *
 * On success, responds `201 Created` with an empty body.
 *
 * | Parameter         | Required | Description        |
 * | ----------------- | -------- | ------------------ |
 * | `name`            | Yes      | Name of the flag.  |
 * | `rule`            | Yes      | Rule of the flag.  |
 */
private fun Route.createFlag() = post {
    val projectId = call.parameters.getOrFail<Long>("id")
    val params = call.receiveParameters()
    val name = params.getOrFail("name")
    val rule = params.getOrFail("rule")

    authorizeForProject(id = projectId, permission = Permission.WRITE)

    if (validateFormula(rule)) {
        val id = database.capturingLastInsertId {
            flags.insert(project_id = projectId, name = name, rule = rule)
        }
        call.response.header(HttpHeaders.Location, PATH_FLAG(id))
        call.respond(HttpStatusCode.Created)
    } else {
        call.respond(HttpStatusCode.BadRequest)
    }
}

/**
 * Lists existing flags for the project.
 *
 * On success, responds `200 OK` with a JSON array containing all flags for the project.
 */
@Suppress("BlockingMethodInNonBlockingContext")
private fun Route.getFlags() = get {
    // Exceptional case, where endpoint is used with token authentication without parameter.
    val projectId = call.parameters["id"]?.toLong()
        ?: call.principal<TokenPrincipal>()?.projectId
        ?: throw MissingRequestParameterException("project_id")

    authorizeForProject(id = projectId, permission = Permission.READ)

    val query = database.flags.selectByProject(projectId)
    val sse = call.request.acceptItems().any {
        ContentType.parse(it.value) == ContentType.Text.EventStream
    }
    if (sse) {
        val channel = produce {
            val flow = query.asFlow().mapToList(application.coroutineContext)
            collectUpdatedFlags(flow) { lastUpdatedAt, updatedFlags ->
                send(
                    SseEvent(
                        id = lastUpdatedAt.epochSeconds.toString(),
                        data = json.encodeToString(updatedFlags)
                    )
                )
            }
        }
        call.stream(HttpStatusCode.OK, channel)
    } else {
        val flags = query.executeAsList()
        call.respond(HttpStatusCode.OK, flags)
    }
}

/**
 * Get an existing flag.
 *
 * On success, responds `200 OK` with a JSON object for the flag.
 *
 * | Parameter | Required | Description     |
 * | --------- | -------- | --------------- |
 * | `id`      | Yes      | ID of the flag. |
 */
private fun Route.getFlag() = get("{id}") {
    val id = call.parameters.getOrFail<Long>("id")
    val flag = database.flags.select(id = id).executeAsOneOrNull() ?: throw NotFoundException()

    authorizeForProject(id = flag.project_id, permission = Permission.READ)

    call.respond(HttpStatusCode.OK, flag)
}

/**
 * Update a flag.
 *
 * On success, responds `204 No Content` with an empty body.
 *
 * | Parameter | Required | Description       |
 * | --------- | -------- | ----------------- |
 * | `id`      | Yes      | ID of the flag.   |
 * | `name`    | No       | Name of the flag. |
 * | `rule`    | No       | Rule of the flag.  |
 */
private fun Route.updateFlag() = put("{id}") {
    val id = call.parameters.getOrFail<Long>("id")
    val params = call.receiveParameters()
    val name = params["name"]
    val rule = params["rule"]

    val flag = database.flags.select(id = id).executeAsOneOrNull() ?: throw NotFoundException()
    authorizeForProject(id = flag.project_id, permission = Permission.WRITE)

    if (rule == null || validateFormula(rule)) {
        database.flags.update(id = id, name = name ?: flag.name, rule = rule ?: flag.rule)
        call.respond(HttpStatusCode.NoContent)
    } else {
        call.respond(HttpStatusCode.BadRequest)
    }
}

/**
 * Evaluates all existing flags for the project.
 *
 * On success, responds `200 OK` with a JSON object mapping flag names to their evaluation.
 *
 * | Parameter         | Required | Description                 |
 * | ----------------- | -------- | --------------------------- |
 * | `project_id`      | Yes      | ID of the project.          |
 * | `env`             | Yes      | Environment for evaluation. |
 */
@Suppress("BlockingMethodInNonBlockingContext")
private fun Route.getFlagsEval() = get(PATH_EVAL) {
    val queryParameters = call.request.queryParameters
    // Exceptional case, where endpoint is used from client SDK without parameter.
    val projectId = queryParameters["id"]?.toLong()
        ?: call.principal<TokenPrincipal>()?.projectId
        ?: throw MissingRequestParameterException("project_id")
    val env = json.decodeFromString<JsonObject>(queryParameters.getOrFail<String>("env"))

    authorizeForProject(id = projectId, permission = Permission.EVAL)

    val query = database.flags.selectByProject(projectId)
    val sse = call.request.acceptItems().any {
        ContentType.parse(it.value) == ContentType.Text.EventStream
    }
    if (sse) {
        val channel = produce {
            val lastFlagsEval = mutableMapOf<String, Boolean?>().withDefault { false }
            val sendUpdatedFlagsEval: suspend (flags: List<Flag>) -> Unit = { flags: List<Flag> ->
                // Select flag evaluations that changed since the last send.
                val updatedFlagsEval = flags.associateBy({ it.name }) {
                    it.isEnabled(env)
                }.filter { (name, enabled) ->
                    val updated = lastFlagsEval[name] != enabled
                    if (updated) {
                        lastFlagsEval[name] = enabled
                    }
                    updated
                }
                if (updatedFlagsEval.isNotEmpty()) {
                    send(
                        SseEvent(
                            id = Clock.System.now().epochSeconds.toString(),
                            data = json.encodeToString(updatedFlagsEval)
                        )
                    )
                }
            }

            // Monitor flag database changes.
            val flow = query.asFlow().mapToList(application.coroutineContext)
            collectUpdatedFlags(flow) { _, updatedFlags ->
                sendUpdatedFlagsEval(updatedFlags)
            }

            // Monitor flag evaluation changes over time.
            while (true) {
                delay(1.minutes.inWholeMilliseconds)
                sendUpdatedFlagsEval(query.executeAsList())
            }
        }
        call.stream(HttpStatusCode.OK, channel)
    } else {
        val flags = query.executeAsList().filter { it.archived_at == null }
        call.respond(
            HttpStatusCode.OK,
            flags.associateBy({ it.name }) {
                it.isEnabled(env)
            }
        )
    }
}

/**
 * Archive a flag.
 *
 * On success, responds `204 No Content` with an empty body.
 *
 * | Parameter | Required | Description     |
 * | --------- | -------- | --------------- |
 * | `id`      | Yes      | ID of the flag. |
 */
private fun Route.archiveFlag() = put {
    val id = call.parameters.getOrFail<Long>("id")

    val flag = database.flags.select(id = id).executeAsOneOrNull() ?: throw NotFoundException()
    authorizeForProject(id = flag.project_id, permission = Permission.WRITE)

    database.flags.archive(id = id)
    call.respond(HttpStatusCode.NoContent)
}

/**
 * Unarchive a flag.
 *
 * On success, responds `204 No Content` with an empty body.
 *
 * | Parameter | Required | Description     |
 * | --------- | -------- | --------------- |
 * | `id`      | Yes      | ID of the flag. |
 */
private fun Route.unarchiveFlag() = delete {
    val id = call.parameters.getOrFail<Long>("id")

    val flag = database.flags.select(id = id).executeAsOneOrNull() ?: throw NotFoundException()
    authorizeForProject(id = flag.project_id, permission = Permission.WRITE)

    database.flags.unarchive(id = id)
    call.respond(HttpStatusCode.NoContent)
}

private fun Flag.isEnabled(env: JsonObject): Boolean? {
    if (archived_at != null) return null
    return doist.ffs.rule.isEnabled(rule, env, id)
}

internal suspend fun collectUpdatedFlags(
    flow: Flow<List<Flag>>,
    collect: suspend (Instant, List<Flag>) -> Unit
) {
    var lastUpdatedFlags = emptyList<Flag>()
    flow.collect { flags ->
        // Select flags that changed since the last flow emission by picking those
        // updated as or more recently than the most recent in the previous batch,
        // that were not contained in the batch itself.
        val lastUpdatedAt =
            lastUpdatedFlags.firstOrNull()?.updated_at ?: Instant.DISTANT_PAST
        val updatedFlags = flags.filter { flag ->
            // Instances are recreated on each call, so we can rely on contains().
            flag.updated_at >= lastUpdatedAt && !lastUpdatedFlags.contains(flag)
        }

        // Send newly updated flags and last updated epoch.
        if (updatedFlags.isNotEmpty()) {
            collect(lastUpdatedAt, updatedFlags)
        }

        // Retain most recently updated flags to exclude in next emission.
        updatedFlags.maxOfOrNull { it.updated_at }?.let { updatedAt ->
            lastUpdatedFlags = updatedFlags.filter { flag ->
                flag.updated_at == updatedAt
            }
        }
    }
}
