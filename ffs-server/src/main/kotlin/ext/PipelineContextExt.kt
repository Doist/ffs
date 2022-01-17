package doist.ffs.ext

import doist.ffs.auth.AccessPrincipal
import doist.ffs.auth.Permission
import doist.ffs.auth.Resource
import doist.ffs.auth.UserPrincipal
import doist.ffs.auth.authorize
import io.ktor.http.URLBuilder
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.application
import io.ktor.server.application.call
import io.ktor.server.auth.principal
import io.ktor.server.resources.href
import io.ktor.util.pipeline.PipelineContext

private typealias PC = PipelineContext<*, ApplicationCall>

/**
 * Ensure access is user with [id].
 */
fun PC.authorizeForUser(id: Long) = authorize(call.principal<UserPrincipal>()?.id == id)

/**
 * Ensure access can access project with [id] with [permission].
 */
fun PC.authorizeForProject(id: Long, permission: Permission) =
    authorize(call.principal<AccessPrincipal>()?.hasAccess(Resource.Project(id), permission))

/**
 * Ensure access can access organization with [id] with [permission].
 */
fun PC.authorizeForOrganization(id: Long, permission: Permission) =
    authorize(call.principal<AccessPrincipal>()?.hasAccess(Resource.Organization(id), permission))

/**
 * Proxy [href] to the underlying [application].
 */
inline fun <reified T : Any> PC.href(resource: T) = application.href(resource)

/**
 * Proxy [href] to the underlying [application].
 */
inline fun <reified T : Any> PC.href(resource: T, builder: URLBuilder) =
    application.href(resource, builder)
