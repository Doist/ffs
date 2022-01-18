@file:Suppress("LocalVariableName", "VariableNaming")

package doist.ffs.routes

import doist.ffs.db.Permission
import doist.ffs.db.capturingLastInsertId
import doist.ffs.db.projects
import doist.ffs.endpoints.Organizations
import doist.ffs.endpoints.Projects
import doist.ffs.ext.authorizeForOrganization
import doist.ffs.ext.authorizeForProject
import doist.ffs.ext.href
import doist.ffs.ext.optionalRoute
import doist.ffs.plugins.database
import io.ktor.http.HttpHeaders
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
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.routing
import io.ktor.server.util.getOrFail
import routes.PATH_LATEST

fun Application.installProjectRoutes() = routing {
    optionalRoute(PATH_LATEST) {
        authenticate("session") {
            createProject()
            getProjects()

            getProject()
            updateProject()
            deleteProject()
        }
    }
}

/**
 * Create a new project.
 */
private fun Route.createProject() = post<Organizations.ById.Projects> { (endpoint) ->
    val organizationId = endpoint.id
    authorizeForOrganization(id = organizationId, permission = Permission.WRITE)

    val params = call.receiveParameters()
    val name = params.getOrFail(Projects.NAME)

    val id = database.capturingLastInsertId {
        projects.insert(organization_id = organizationId, name = name)
    }
    call.response.header(HttpHeaders.Location, href(Projects.ById(id = id)))
    call.respond(HttpStatusCode.Created)
}

/**
 * Lists existing projects for the organization.
 */
private fun Route.getProjects() = get<Organizations.ById.Projects> { (endpoint) ->
    val organizationId = endpoint.id
    authorizeForOrganization(id = organizationId, permission = Permission.READ)

    val projects = database.projects.selectByOrganization(
        organization_id = organizationId
    ).executeAsList()
    call.respond(HttpStatusCode.OK, projects)
}

/**
 * Get an existing project.
 */
private fun Route.getProject() = get<Projects.ById> { (_, id) ->
    authorizeForProject(id = id, permission = Permission.READ)

    val project = database.projects.select(id = id).executeAsOneOrNull()
        ?: throw NotFoundException()
    call.respond(HttpStatusCode.OK, project)
}

/**
 * Update a project.
 */
private fun Route.updateProject() = put<Projects.ById> { (_, id) ->
    authorizeForProject(id = id, permission = Permission.WRITE)

    val params = call.receiveParameters()
    val name = params[Projects.NAME]

    database.projects.run {
        val project = select(id = id).executeAsOneOrNull() ?: throw NotFoundException()
        update(id = id, name = name ?: project.name)
    }
    call.respond(HttpStatusCode.NoContent)
}

/**
 * Delete a project.
 *
 * On success, responds `204 No Content` with an empty body.
 */
private fun Route.deleteProject() = delete<Projects.ById> { (_, id) ->
    authorizeForProject(id = id, permission = Permission.DELETE)

    database.projects.delete(id = id)
    call.respond(HttpStatusCode.NoContent)
}
