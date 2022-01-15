package routes

import doist.ffs.auth.Permission
import doist.ffs.db.tokens
import doist.ffs.ext.authorizeForProject
import doist.ffs.ext.optionalRoute
import doist.ffs.plugins.database
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.util.getOrFail

const val PATH_TOKENS = "/tokens"

@Suppress("FunctionName")
fun PATH_TOKEN(id: Any) = "$PATH_TOKENS/$id"

fun Application.installTokenRoutes() = routing {
    optionalRoute(PATH_LATEST) {
        route(PATH_TOKENS) {
            authenticate("session") {
                updateToken()
                deleteToken()
            }
        }
    }
}

/**
 * Update description of token for project.
 *
 * On success, responds `204 No Content` with an empty body.
 *
 * | Parameter      | Required | Description                |
 * | -------------- | -------- | -------------------------- |
 * | `description`  | Yes      | Description of the token.  |
 */
private fun Route.updateToken() = put("{id}") {
    val id = call.parameters.getOrFail<Long>("id")
    val params = call.receiveParameters()
    val description = params.getOrFail("description")

    val projectId = database.tokens.selectProjectIdById(id = id).executeAsOne()
    authorizeForProject(id = projectId, permission = Permission.WRITE)

    database.tokens.update(id = id, description = description)
    call.respond(HttpStatusCode.NoContent)
}

/**
 * Delete token for project.
 *
 * On success, responds `204 No Content` with an empty body.
 */
private fun Route.deleteToken() = delete("{id}") {
    val id = call.parameters.getOrFail<Long>("id")

    val projectId = database.tokens.selectProjectIdById(id = id).executeAsOne()
    authorizeForProject(id = projectId, permission = Permission.WRITE)

    database.tokens.delete(id = id)
    call.respond(HttpStatusCode.NoContent)
}
