package doist.ffs.routes

import doist.ffs.capturingLastInsertId
import doist.ffs.organizations
import doist.ffs.withDatabase
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.features.NotFoundException
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveParameters
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.put
import io.ktor.routing.routing
import io.ktor.util.getOrFail
import kotlinx.serialization.ExperimentalSerializationApi

fun Application.organizationRoutes() {
    routing {
        routeCreateOrganization()
        routeGetOrganizations()
        routeGetOrganization()
        routeUpdateOrganization()
        routeDeleteOrganization()
    }
}

const val PATH_ORGANIZATIONS = "/organizations"
const val PATH_ORGANIZATION = "/organizations/{id}"

@Suppress("FunctionName")
fun PATH_ORGANIZATION(id: Any) = PATH_ORGANIZATION.replace("{id}", id.toString())

/**
 * Create a new organization.
 *
 * On success, responds `201 Created` with an empty body.
 *
 * | Parameter | Required | Description               |
 * | --------- | -------- | ------------------------- |
 * | `name`    | Yes      | Name of the organization. |
 */
fun Route.routeCreateOrganization() = post(PATH_ORGANIZATIONS) {
    val name = call.receiveParameters().getOrFail("name")
    val id = withDatabase { db ->
        db.capturingLastInsertId {
            db.organizations.insert(name)
        }
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
fun Route.routeGetOrganizations() = get(PATH_ORGANIZATIONS) {
    val organizations = withDatabase { db ->
        db.organizations.selectAll().executeAsList()
    }
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
@OptIn(ExperimentalSerializationApi::class)
fun Route.routeGetOrganization() = get(PATH_ORGANIZATION) {
    val id = call.parameters.getOrFail<Long>("id")
    val organization = withDatabase { db ->
        db.organizations.select(id = id).executeAsOneOrNull()
    } ?: throw NotFoundException()
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
fun Route.routeUpdateOrganization() = put(PATH_ORGANIZATION) {
    val id = call.parameters.getOrFail<Long>("id")
    val name = call.receiveParameters()["name"]
    withDatabase { db ->
        val organization = db.organizations.select(id = id).executeAsOneOrNull()
            ?: throw NotFoundException()
        db.organizations.update(id = id, name = name ?: organization.name)
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
fun Route.routeDeleteOrganization() = delete(PATH_ORGANIZATION) {
    val id = call.parameters.getOrFail<Long>("id")
    withDatabase { db ->
        db.organizations.delete(id = id)
    }
    call.respond(HttpStatusCode.NoContent)
}
