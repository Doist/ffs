package routes

import doist.ffs.auth.Permission
import doist.ffs.db.TokenGenerator
import doist.ffs.db.organizations
import doist.ffs.db.tokens
import doist.ffs.ext.authorizeForOrganization
import doist.ffs.ext.authorizeForProject
import doist.ffs.ext.optionalRoute
import doist.ffs.plugins.database
import doist.ffs.routes.PATH_PROJECTS
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.util.getOrFail

const val PATH_TOKENS = "/tokens"

@Suppress("FunctionName")
fun PATH_TOKEN(id: Any) = "$PATH_TOKENS/$id"

fun Application.installTokenRoutes() = routing {
    optionalRoute(PATH_LATEST) {
        route("$PATH_PROJECTS/{id}/$PATH_TOKENS") {
            authenticate("session") {
                createToken()
                getTokens()
            }
        }

        route(PATH_TOKENS) {
            authenticate("session") {
                updateToken()
                deleteToken()
            }
        }
    }
}

/**
 * Generate token for project.
 */
private fun Route.createToken() = post {
    val projectId = call.parameters.getOrFail<Long>("id")
    val params = call.receiveParameters()
    val permission = Permission.valueOf(params.getOrFail("permission").uppercase())
    val description = params.getOrFail("description")

    authorizeForProject(id = projectId, permission = Permission.WRITE)

    val token = TokenGenerator.generate(permission)
    database.tokens.insert(project_id = projectId, token = token, description = description)
    call.respond(HttpStatusCode.Created, token)
}

/**
 * Get all tokens for project.
 *
 * On success, responds `200 OK` with a JSON array containing all tokens for the project.
 */
private fun Route.getTokens() = get {
    val projectId = call.parameters.getOrFail<Long>("id")

    authorizeForOrganization(id = projectId, permission = Permission.READ)

    val tokens = database.tokens.selectByProject(project_id = projectId).executeAsList()
    call.respond(HttpStatusCode.OK, tokens)
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
