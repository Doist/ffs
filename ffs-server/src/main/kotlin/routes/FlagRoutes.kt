@file:Suppress("LocalVariableName", "VariableNaming", "TooManyFunctions")

package doist.ffs.routes

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import doist.ffs.auth.TokenPrincipal
import doist.ffs.db.Flag
import doist.ffs.db.Permission
import doist.ffs.db.capturingLastInsertId
import doist.ffs.db.flags
import doist.ffs.endpoints.Flags
import doist.ffs.endpoints.Projects
import doist.ffs.ext.authorizeForProject
import doist.ffs.ext.href
import doist.ffs.ext.optionalRoute
import doist.ffs.ext.stream
import doist.ffs.plugins.database
import doist.ffs.rule.validateFormula
import doist.ffs.serialization.json
import doist.ffs.sse.LastEventID
import doist.ffs.sse.SseEvent
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.plugins.MissingRequestParameterException
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.request.acceptItems
import io.ktor.server.request.header
import io.ktor.server.request.receiveParameters
import io.ktor.server.resources.delete
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.routing
import io.ktor.server.util.getOrFail
import io.ktor.util.pipeline.PipelineContext
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

fun Application.installFlagRoutes() = routing {
    optionalRoute(PATH_LATEST) {
        authenticate("session") {
            createFlag()
            getFlags()

            getFlag()
            updateFlag()

            archiveFlag()
            unarchiveFlag()
        }

        authenticate("token") {
            getFlagsViaToken()
            getFlagsEvalViaToken()
        }
    }
}

/**
 * Creates a new flag.
 */
private fun Route.createFlag() = post<Projects.ById.Flags> { (endpoint) ->
    val projectId = endpoint.id
    authorizeForProject(id = projectId, permission = Permission.WRITE)

    val params = call.receiveParameters()
    val name = params.getOrFail(Flags.NAME)
    val rule = params.getOrFail(Flags.RULE)

    if (validateFormula(rule)) {
        val id = database.capturingLastInsertId {
            flags.insert(project_id = projectId, name = name, rule = rule)
        }
        call.response.header(HttpHeaders.Location, href(Flags.ById(id = id)))
        call.respond(HttpStatusCode.Created)
    } else {
        call.respond(HttpStatusCode.BadRequest)
    }
}

/**
 * Lists existing flags for a project.
 */
private fun Route.getFlags() = get<Projects.ById.Flags> { (endpoint) ->
    getFlags(this, endpoint.id)
}

/**
 * Get flags for token associated with the token's project.
 */
private fun Route.getFlagsViaToken() = get<Flags> {
    val id = call.principal<TokenPrincipal>()?.projectId
        ?: throw MissingRequestParameterException("id")
    getFlags(this, id)
}

private suspend fun getFlags(ctx: PipelineContext<Unit, ApplicationCall>, projectId: Long) {
    ctx.authorizeForProject(id = projectId, permission = Permission.READ)

    val query = ctx.database.flags.selectByProject(projectId)
    val request = ctx.call.request
    val sse = request.acceptItems().any {
        ContentType.parse(it.value) == ContentType.Text.EventStream
    }
    if (sse) {
        val since = request.header(HttpHeaders.LastEventID)?.toLongOrNull()?.let { lastId ->
            Instant.fromEpochSeconds(lastId)
        } ?: Instant.DISTANT_PAST
        val channel = ctx.produce {
            val flow = query.asFlow().mapToList(ctx.application.coroutineContext)
            collectUpdatedFlags(flow, since) { lastUpdatedAt, updatedFlags ->
                send(
                    SseEvent(
                        id = lastUpdatedAt.epochSeconds.toString(),
                        data = json.encodeToString(updatedFlags)
                    )
                )
            }
        }
        ctx.call.stream(HttpStatusCode.OK, channel)
    } else {
        val flags = query.executeAsList()
        ctx.call.respond(HttpStatusCode.OK, flags)
    }
}

/**
 * Get an existing flag.
 */
private fun Route.getFlag() = get<Flags.ById> { (_, id) ->
    val flag = database.flags.select(id = id).executeAsOneOrNull() ?: throw NotFoundException()
    authorizeForProject(id = flag.project_id, permission = Permission.READ)

    call.respond(HttpStatusCode.OK, flag)
}

/**
 * Update a flag.
 */
private fun Route.updateFlag() = put<Flags.ById> { (_, id) ->
    val flag = database.flags.select(id = id).executeAsOneOrNull() ?: throw NotFoundException()
    authorizeForProject(id = flag.project_id, permission = Permission.WRITE)

    val params = call.receiveParameters()
    val name = params[Flags.NAME]
    val rule = params[Flags.RULE]

    if (rule == null || validateFormula(rule)) {
        database.flags.update(id = id, name = name ?: flag.name, rule = rule ?: flag.rule)
        call.respond(HttpStatusCode.NoContent)
    } else {
        call.respond(HttpStatusCode.BadRequest)
    }
}

/**
 * Archive a flag.
 */
private fun Route.archiveFlag() = put<Flags.ById.Archive> { (endpoint) ->
    val id = endpoint.id
    val flag = database.flags.select(id = id).executeAsOneOrNull() ?: throw NotFoundException()
    authorizeForProject(id = flag.project_id, permission = Permission.WRITE)

    database.flags.archive(id = id)
    call.respond(HttpStatusCode.NoContent)
}

/**
 * Unarchive a flag.
 */
private fun Route.unarchiveFlag() = delete<Flags.ById.Archive> { (endpoint) ->
    val id = endpoint.id
    val flag = database.flags.select(id = id).executeAsOneOrNull() ?: throw NotFoundException()
    authorizeForProject(id = flag.project_id, permission = Permission.WRITE)

    database.flags.unarchive(id = id)
    call.respond(HttpStatusCode.NoContent)
}

/**
 * Evaluates flags for the token's project.
 */
private fun Route.getFlagsEvalViaToken() = get<Flags.Eval> {
    val projectId = call.principal<TokenPrincipal>()!!.projectId
    authorizeForProject(id = projectId, permission = Permission.EVAL)

    val env = json.decodeFromString<JsonObject>(
        call.request.queryParameters.getOrFail<String>(Flags.ENV)
    )

    val query = database.flags.selectByProject(projectId)
    val request = call.request
    val sse = call.request.acceptItems().any {
        ContentType.parse(it.value) == ContentType.Text.EventStream
    }
    if (sse) {
        val since = request.header(HttpHeaders.LastEventID)?.toLongOrNull()?.let { lastId ->
            Instant.fromEpochSeconds(lastId)
        } ?: Instant.DISTANT_PAST
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
            collectUpdatedFlags(flow, since) { _, updatedFlags ->
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

private suspend fun collectUpdatedFlags(
    flow: Flow<List<Flag>>,
    since: Instant,
    collect: suspend (Instant, List<Flag>) -> Unit
) {
    var lastUpdatedFlags = emptyList<Flag>()
    flow.collect { flags ->
        // Select flags that changed since the last flow emission by picking those
        // updated as or more recently than the most recent in the previous batch,
        // that were not contained in the batch itself.
        val lastUpdatedAt = lastUpdatedFlags.firstOrNull()?.updated_at ?: since
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

private fun Flag.isEnabled(env: JsonObject): Boolean? {
    if (archived_at != null) return null
    return doist.ffs.rule.isEnabled(rule, env, id)
}
