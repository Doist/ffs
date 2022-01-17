@file:Suppress("LocalVariableName", "VariableNaming")

package doist.ffs.routes

import doist.ffs.auth.Permission
import doist.ffs.db.capturingLastInsertId
import doist.ffs.db.projects
import doist.ffs.ext.authorizeForOrganization
import doist.ffs.ext.authorizeForProject
import doist.ffs.ext.optionalRoute
import doist.ffs.plugins.database
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.NotFoundException
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
import routes.PATH_LATEST

const val PATH_PROJECTS = "/projects"

@Suppress("FunctionName")
fun PATH_PROJECT(id: Any) = "$PATH_PROJECTS/$id"

fun Application.installProjectRoutes() = routing {
    optionalRoute(PATH_LATEST) {
        route("/organizations/{id}/$PATH_PROJECTS") {
            authenticate("session") {
                createProject()
                getProjects()
            }
        }

        route(PATH_PROJECTS) {
            authenticate("session") {
                getProject()
                updateProject()
                deleteProject()
            }
        }
    }
}

/**
 * Create a new project.
 */
private fun Route.createProject() = post {
    val organizationId = call.parameters.getOrFail<Long>("id")
    val params = call.receiveParameters()
    val name = params.getOrFail("name")

    authorizeForOrganization(id = organizationId, permission = Permission.WRITE)

    val id = database.capturingLastInsertId {
        projects.insert(organization_id = organizationId, name = name)
    }
    call.response.header(HttpHeaders.Location, PATH_PROJECT(id))
    call.respond(HttpStatusCode.Created)
}

/**
 * Lists existing projects for the organization.
 */
private fun Route.getProjects() = get {
    val organizationId = call.parameters.getOrFail<Long>("id")

    authorizeForOrganization(id = organizationId, permission = Permission.READ)

    val projects = database.projects.selectByOrganization(
        organization_id = organizationId
    ).executeAsList()
    call.respond(HttpStatusCode.OK, projects)
}

/**
 * Get an existing project.
 */
private fun Route.getProject() = get("{id}") {
    val id = call.parameters.getOrFail<Long>("id")

    authorizeForProject(id = id, permission = Permission.READ)

    val project = database.projects.select(id = id).executeAsOneOrNull()
        ?: throw NotFoundException()
    call.respond(HttpStatusCode.OK, project)
}

/**
 * Update a project.
 */
private fun Route.updateProject() = put("{id}") {
    val id = call.parameters.getOrFail<Long>("id")
    val name = call.receiveParameters()["name"]

    authorizeForProject(id = id, permission = Permission.WRITE)

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
private fun Route.deleteProject() = delete("{id}") {
    val id = call.parameters.getOrFail<Long>("id")

    authorizeForProject(id = id, permission = Permission.DELETE)

    database.projects.delete(id = id)
    call.respond(HttpStatusCode.NoContent)
}
