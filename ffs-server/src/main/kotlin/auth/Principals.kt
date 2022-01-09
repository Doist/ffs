package doist.ffs.auth

import io.ktor.server.auth.Principal

/**
 * An authenticated access to specific resources/permissions.
 */
abstract class AccessPrincipal(val accesses: Collection<Access>) : Principal {
    constructor(resource: Resource, permissions: Collection<Permission>) :
        this(listOf(Access(resource, permissions)))

    fun hasAccess(resource: Resource, permission: Permission) = accesses.any {
        it.resource == resource && it.permissions.contains(permission)
    }

    inline fun <reified T : Resource> getPermissions(): Map<Long, Collection<Permission>> =
        accesses.asSequence()
            .filter { it.resource is T }
            .associate { it.resource.id to it.permissions }
}

/**
 * An authenticated API access.
 */
data class TokenPrincipal(
    val projectId: Long,
    val permission: Permission
) : AccessPrincipal(Resource.Project(projectId), listOf(permission))

/**
 * An authenticated user.
 */
data class UserPrincipal(
    val id: Long,
    val organizationPermissions: Map<Long, Collection<Permission>>,
    val projectPermissions: Map<Long, Collection<Permission>>
) : AccessPrincipal(
    organizationPermissions.map { (organizationId, permissions) ->
        Access(Resource.Organization(organizationId), permissions)
    } + projectPermissions.map { (projectId, permissions) ->
        Access(Resource.Project(projectId), permissions)
    }
)
