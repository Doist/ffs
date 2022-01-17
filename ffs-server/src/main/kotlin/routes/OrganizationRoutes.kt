package doist.ffs.routes

import doist.ffs.auth.Permission
import doist.ffs.auth.UserPrincipal
import doist.ffs.db.RoleEnum
import doist.ffs.db.capturingLastInsertId
import doist.ffs.db.members
import doist.ffs.db.organizations
import doist.ffs.endpoints.Organizations
import doist.ffs.ext.authorizeForOrganization
import doist.ffs.ext.authorizeForUser
import doist.ffs.ext.href
import doist.ffs.ext.optionalRoute
import doist.ffs.plugins.database
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
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

fun Application.installOrganizationRoutes() = routing {
    optionalRoute(PATH_LATEST) {
        authenticate("session") {
            createOrganization()
            getOrganizations()
            getOrganization()
            updateOrganization()
            deleteOrganization()

            addUser()
            updateUser()
            removeUser()
        }
    }
}

/**
 * Create a new organization. The requesting user becomes an admin.
 */
private fun Route.createOrganization() = post<Organizations> {
    val userId = call.principal<UserPrincipal>()!!.id
    val name = call.receiveParameters().getOrFail(Organizations.NAME)

    val id = database.run {
        transactionWithResult<Long> {
            capturingLastInsertId {
                organizations.insert(name)
            }.also {
                members.insert(user_id = userId, organization_id = it, role = RoleEnum.ADMIN)
            }
        }
    }

    call.response.header(HttpHeaders.Location, href(Organizations.ById(id = id)))
    call.respond(HttpStatusCode.Created)
}

/**
 * Lists organizations for the current user.
 */
private fun Route.getOrganizations() = get<Organizations> {
    val userId = call.principal<UserPrincipal>()!!.id
    authorizeForUser(id = userId)

    val organizations = database.members.selectOrganizationByUserId(
        user_id = userId
    ).executeAsList()
    call.respond(HttpStatusCode.OK, organizations)
}

/**
 * Get an existing organization.
 */
private fun Route.getOrganization() = get<Organizations.ById> { (_, id) ->
    authorizeForOrganization(id, Permission.READ)

    val organization = database.organizations.select(id = id).executeAsOneOrNull()
        ?: throw NotFoundException()
    call.respond(HttpStatusCode.OK, organization)
}

/**
 * Update an organization.
 */
private fun Route.updateOrganization() = put<Organizations.ById> { (_, id) ->
    authorizeForOrganization(id, Permission.WRITE)

    val params = call.receiveParameters()
    val name = params[Organizations.NAME]

    database.organizations.run {
        val organization = select(id = id).executeAsOneOrNull() ?: throw NotFoundException()
        update(id = id, name = name ?: organization.name)
    }
    call.respond(HttpStatusCode.NoContent)
}

/**
 * Delete an organization.
 */
private fun Route.deleteOrganization() = delete<Organizations.ById> { (_, id) ->
    authorizeForOrganization(id, Permission.DELETE)

    database.organizations.delete(id = id)
    call.respond(HttpStatusCode.NoContent)
}

/**
 * Add user to an organization.
 */
private fun Route.addUser() = post<Organizations.ById.Members.ById> { (endpoint, userId) ->
    val id = endpoint.parent.id
    authorizeForOrganization(id, Permission.DELETE)

    val params = call.receiveParameters()
    val role = RoleEnum.valueOf(params.getOrFail(Organizations.ROLE).uppercase())

    database.members.insert(user_id = userId, organization_id = id, role = role)
    call.respond(HttpStatusCode.Created)
}

/**
 * Update user role within organization.
 */
private fun Route.updateUser() = put<Organizations.ById.Members.ById> { (endpoint, userId) ->
    val id = endpoint.parent.id
    authorizeForOrganization(id, Permission.DELETE)

    val params = call.receiveParameters()
    val role = RoleEnum.valueOf(params.getOrFail(Organizations.ROLE).uppercase())

    database.members.update(user_id = userId, organization_id = id, role = role)
    call.respond(HttpStatusCode.NoContent)
}

/**
 * Remove user from organization.
 */
private fun Route.removeUser() = delete<Organizations.ById.Members.ById> { (endpoint, userId) ->
    val id = endpoint.parent.id
    authorizeForOrganization(id, Permission.DELETE)

    database.members.delete(user_id = userId, organization_id = id)
    call.respond(HttpStatusCode.NoContent)
}
