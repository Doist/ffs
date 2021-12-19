package doist.ffs.routes

import doist.ffs.db.capturingLastInsertId
import doist.ffs.db.organizations
import doist.ffs.plugins.database
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.application
import io.ktor.server.application.call
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

const val PATH_ORGANIZATIONS = "/organizations"

@Suppress("FunctionName")
fun PATH_ORGANIZATION(id: Any) = "$PATH_ORGANIZATIONS/$id"

fun Application.installOrganizationRoutes() {
    routing {
        route(PATH_ORGANIZATIONS) {
            createOrganization()
            getOrganizations()
            getOrganization()
            updateOrganization()
            deleteOrganization()
        }
    }
}

/**
 * Create a new organization.
 *
 * On success, responds `201 Created` with an empty body.
 *
 * | Parameter | Required | Description               |
 * | --------- | -------- | ------------------------- |
 * | `name`    | Yes      | Name of the organization. |
 */
private fun Route.createOrganization() = post {
    val name = call.receiveParameters().getOrFail("name")
    val id = application.database.capturingLastInsertId {
        organizations.insert(name)
    }
    call.run {
        response.header(HttpHeaders.Location, PATH_ORGANIZATION(id))
        respond(HttpStatusCode.Created)
    }
}

/**
 * Lists existing organizations.
 *
 * On success, responds `200 OK` with a JSON array containing all organizations.
 */
private fun Route.getOrganizations() = get {
    val organizations = application.database.organizations.selectAll().executeAsList()
    call.respond(HttpStatusCode.OK, organizations)
}

/**
 * Get an existing organization.
 *
 * On success, responds `200 OK` with a JSON object for the organization.
 *
 * | Parameter | Required | Description             |
 * | --------- | -------- | ----------------------- |
 * | `id`      | Yes      | ID of the organization. |
 */
private fun Route.getOrganization() = get("{id}") {
    val id = call.parameters.getOrFail<Long>("id")
    val organization = application.database.organizations.select(id = id).executeAsOneOrNull()
        ?: throw NotFoundException()
    call.respond(HttpStatusCode.OK, organization)
}

/**
 * Update an organization.
 *
 * On success, responds `200 OK` with an empty body.
 *
 * | Parameter | Required | Description               |
 * | --------- | -------- | ------------------------- |
 * | `id`      | Yes      | ID of the organization.   |
 * | `name`    | No       | Name of the organization. |
 */
private fun Route.updateOrganization() = put("{id}") {
    val id = call.parameters.getOrFail<Long>("id")
    val name = call.receiveParameters()["name"]
    application.database.organizations.run {
        val organization = select(id = id).executeAsOneOrNull() ?: throw NotFoundException()
        update(id = id, name = name ?: organization.name)
    }
    call.respond(HttpStatusCode.NoContent)
}

/**
 * Delete an organization.
 *
 * On success, responds `201 Created` with an empty body.
 *
 * | Parameter | Required | Description             |
 * | --------- | -------- | ----------------------- |
 * | `id`      | Yes      | ID of the organization. |
 */
private fun Route.deleteOrganization() = delete("{id}") {
    val id = call.parameters.getOrFail<Long>("id")
    application.database.organizations.delete(id = id)
    call.respond(HttpStatusCode.NoContent)
}
