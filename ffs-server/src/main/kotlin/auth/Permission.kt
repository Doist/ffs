package doist.ffs.auth

import doist.ffs.db.RoleEnum

enum class Permission {
    /**
     * Eval permission allows evaluating the resource without reading it (typically, flags).
     */
    EVAL,

    /**
     * Read permission allows accessing the resource.
     */
    READ,

    /**
     * Write permission allows accessing and modifying the resource.
     */
    WRITE,

    /**
     * Delete permission allows irreversibly deleting the resource.
     */
    DELETE;

    // Declare companion object to allow extending it.
    companion object
}

val RoleEnum.permissions: Collection<Permission>
    get() = when (this) {
        RoleEnum.ADMIN -> listOf(
            Permission.EVAL, Permission.READ, Permission.WRITE, Permission.DELETE
        )
        RoleEnum.USER -> listOf(
            Permission.EVAL, Permission.READ, Permission.WRITE
        )
        RoleEnum.READER -> listOf(
            Permission.READ
        )
    }
