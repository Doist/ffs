package doist.ffs.routes

import doist.ffs.capturingLastInsertId
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
import io.ktor.util.getValue
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

/**
 * Create a new organization.
 *
 * On success, responds `201 Created` with an empty body.
 *
 * | Parameter | Required | Description               |
 * | --------- | -------- | ------------------------- |
 * | `name`    | Yes      | Name of the organization. |
 */
fun Route.routeCreateOrganization() = post("/organizations") {
    val parameters = call.receiveParameters()
    val name: String by parameters
    val id = withDatabase { db ->
        db.capturingLastInsertId {
            db.organizationQueries.insert(name)
        }
    }
    call.run {
        response.header(HttpHeaders.Location, "/organizations/$id")
        respond(HttpStatusCode.Created)
    }
}

/**
 * Lists existing organizations.
 *
 * On success, responds `200 OK` with a JSON array containing all organizations.
 */
fun Route.routeGetOrganizations() = get("/organizations") {
    val organizations = withDatabase { db ->
        db.organizationQueries.selectAll().executeAsList()
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
fun Route.routeGetOrganization() = get("/organizations/{id}") {
    val id: Long by call.parameters
    val organization = withDatabase { db ->
        db.organizationQueries.select(id = id).executeAsOneOrNull()
    } ?: throw NotFoundException()
    call.respond(HttpStatusCode.OK, organization)
}

/**
 * Update an organization.
 *
 * On success, responds `200 OK` with an empty body..
 *
 * | Parameter | Required | Description               |
 * | --------- | -------- | ------------------------- |
 * | `id`      | Yes      | ID of the organization.   |
 * | `name`    | No       | Name of the organization. |
 */
fun Route.routeUpdateOrganization() = put("/organizations/{id}") {
    val id: Long by call.parameters
    val name: String? by call.receiveParameters()
    withDatabase { db ->
        val organization = db.organizationQueries.select(id = id).executeAsOneOrNull()
            ?: throw NotFoundException()
        db.organizationQueries.update(id = id, name = name ?: organization.name)
    }
    call.respond(HttpStatusCode.NoContent)
}

/**
 * Delete an organization.
 *
 * On success, responds `201 Created` with an empty body.
 *
 * | Parameter | Required | Description               |
 * | --------- | -------- | ------------------------- |
 * | `id`      | Yes      | ID of the organization.   |
 */
fun Route.routeDeleteOrganization() = delete("/organizations/{id}") {
    val id: Long by call.parameters
    withDatabase { db ->
        db.organizationQueries.delete(id = id)
    }
    call.respond(HttpStatusCode.NoContent)
}
