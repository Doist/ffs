package doist.ffs.ext

import doist.ffs.auth.AccessPrincipal
import doist.ffs.auth.Permission
import doist.ffs.auth.Resource
import doist.ffs.auth.UserPrincipal
import doist.ffs.auth.authorize
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.principal
import io.ktor.util.pipeline.PipelineContext

/**
 * Ensure access is user with [id].
 */
fun PipelineContext<*, ApplicationCall>.authorizeForUser(id: Long) {
    authorize(call.principal<UserPrincipal>()?.id == id)
}

/**
 * Ensure access can access project with [id] with [permission].
 */
fun PipelineContext<*, ApplicationCall>.authorizeForProject(id: Long, permission: Permission) {
    authorize(call.principal<AccessPrincipal>()?.hasAccess(Resource.Project(id), permission))
}

/**
 * Ensure access can access organization with [id] with [permission].
 */
fun PipelineContext<*, ApplicationCall>.authorizeForOrganization(id: Long, permission: Permission) {
    authorize(call.principal<AccessPrincipal>()?.hasAccess(Resource.Organization(id), permission))
}
