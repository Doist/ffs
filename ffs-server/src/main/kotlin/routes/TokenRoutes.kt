package routes

import doist.ffs.auth.Permission
import doist.ffs.db.TokenGenerator
import doist.ffs.db.tokens
import doist.ffs.endpoints.Projects
import doist.ffs.endpoints.Tokens
import doist.ffs.ext.authorizeForOrganization
import doist.ffs.ext.authorizeForProject
import doist.ffs.ext.optionalRoute
import doist.ffs.plugins.database
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.request.receiveParameters
import io.ktor.server.resources.delete
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.routing
import io.ktor.server.util.getOrFail

fun Application.installTokenRoutes() = routing {
    optionalRoute(PATH_LATEST) {
        authenticate("session") {
            createToken()
            getTokens()

            updateToken()
            deleteToken()
        }
    }
}

/**
 * Generate token for project.
 */
private fun Route.createToken() = post<Projects.ById.Tokens> { (endpoint) ->
    val projectId = endpoint.id
    authorizeForProject(id = projectId, permission = Permission.WRITE)

    val params = call.receiveParameters()
    val permission = Permission.valueOf(params.getOrFail(Tokens.PERMISSION).uppercase())
    val description = params.getOrFail(Tokens.DESCRIPTION)

    val token = TokenGenerator.generate(permission)
    database.tokens.insert(project_id = projectId, token = token, description = description)
    call.respond(HttpStatusCode.Created, token)
}

/**
 * Get all tokens for project.
 *
 * On success, responds `200 OK` with a JSON array containing all tokens for the project.
 */
private fun Route.getTokens() = get<Projects.ById.Tokens> { (endpoint) ->
    val projectId = endpoint.id
    authorizeForOrganization(id = projectId, permission = Permission.READ)

    val tokens = database.tokens.selectByProject(project_id = projectId).executeAsList()
    call.respond(HttpStatusCode.OK, tokens)
}

/**
 * Update description of token for project.
 */
private fun Route.updateToken() = put<Tokens.ById> { (_, id) ->
    val projectId = database.tokens.selectProjectIdById(id = id).executeAsOne()
    authorizeForProject(id = projectId, permission = Permission.WRITE)

    val params = call.receiveParameters()
    val description = params[Tokens.DESCRIPTION]

    database.tokens.run {
        val token = select(id = id).executeAsOneOrNull() ?: throw NotFoundException()
        update(id = id, description = description ?: token.description)
    }
    call.respond(HttpStatusCode.NoContent)
}

/**
 * Delete token for project.
 *
 * On success, responds `204 No Content` with an empty body.
 */
private fun Route.deleteToken() = delete<Tokens.ById> { (_, id) ->
    val projectId = database.tokens.selectProjectIdById(id = id).executeAsOne()
    authorizeForProject(id = projectId, permission = Permission.WRITE)

    database.tokens.delete(id = id)
    call.respond(HttpStatusCode.NoContent)
}
