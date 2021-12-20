@file:Suppress("LocalVariableName", "VariableNaming")

package doist.ffs.routes

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import doist.ffs.db.Flag
import doist.ffs.db.capturingLastInsertId
import doist.ffs.db.flags
import doist.ffs.plugins.database
import doist.ffs.serialization.SseEvent
import doist.ffs.serialization.json
import doist.ffs.serialization.respondSse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.application
import io.ktor.server.application.call
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
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Duration.Companion.minutes

const val PATH_FLAGS = "/flags"
const val PATH_EVAL = "/eval"

@Suppress("FunctionName")
fun PATH_FLAG(id: Any) = "$PATH_FLAGS/$id"

fun Application.installFlagRoutes() {
    routing {
        route(PATH_FLAGS) {
            createFlag()
            getFlags()
            getFlag()
            updateFlag()

            getFlagsEval()
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
 * | `project_id`      | Yes      | ID of the project. |
 * | `name`            | Yes      | Name of the flag.  |
 * | `rule`            | Yes      | Rule of the flag.  |
 */
private fun Route.createFlag() = post {
    val params = call.receiveParameters()
    val projectId = params.getOrFail<Long>("project_id")
    val name = params.getOrFail("name")
    val rule = params.getOrFail("rule")
    val id = application.database.capturingLastInsertId {
        flags.insert(project_id = projectId, name = name, rule = rule)
    }
    call.run {
        response.header(HttpHeaders.Location, PATH_FLAG(id))
        respond(HttpStatusCode.Created)
    }
}

/**
 * Lists existing flags for the project.
 *
 * On success, responds `200 OK` with a JSON array containing all flags for the project.
 *
 * | Parameter         | Required | Description        |
 * | ----------------- | -------- | ------------------ |
 * | `project_id`      | Yes      | ID of the project. |
 */
@Suppress("BlockingMethodInNonBlockingContext")
private fun Route.getFlags() = get {
    val projectId = call.request.queryParameters.getOrFail<Long>("project_id")
    val query = application.database.flags.selectByProject(projectId)
    val sse = call.request.acceptItems().any { ContentType.Text.EventStream.match(it.value) }
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
        call.respondSse(HttpStatusCode.OK, channel)
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
    val project = application.database.flags.select(id = id).executeAsOneOrNull()
        ?: throw NotFoundException()
    call.respond(HttpStatusCode.OK, project)
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
    application.database.flags.run {
        val flag = select(id = id).executeAsOneOrNull() ?: throw NotFoundException()
        update(id = id, name = name ?: flag.name, rule = rule ?: flag.rule)
    }
    call.respond(HttpStatusCode.NoContent)
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
    val projectId = queryParameters.getOrFail<Long>("project_id")
    val env = json.decodeFromString<JsonObject>(queryParameters.getOrFail<String>("env"))
    val query = application.database.flags.selectByProject(projectId)
    val sse = call.request.acceptItems().any { ContentType.Text.EventStream.match(it.value) }
    if (sse) {
        val channel = produce {
            val lastFlagsEval = mutableMapOf<String, Boolean>().withDefault { false }
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
        call.respondSse(HttpStatusCode.OK, channel)
    } else {
        val flags = query.executeAsList()
        call.respond(
            HttpStatusCode.OK,
            flags.associateBy({ it.name }) {
                it.isEnabled(env)
            }
        )
    }
}

private fun Flag.isEnabled(env: JsonObject): Boolean {
    val rolloutId = env["rollout.id"]?.jsonPrimitive?.contentOrNull
        ?: throw IllegalArgumentException("env[\"rollout.id\"] is missing or incorrectly specified")
    return doist.ffs.rule.isEnabled(rule, env, "${id}$rolloutId")
}

private suspend fun collectUpdatedFlags(
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
