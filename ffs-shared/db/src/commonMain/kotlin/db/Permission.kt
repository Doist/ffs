package doist.ffs.db

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

val Role.permissions: Collection<Permission>
    get() = when (this) {
        Role.ADMIN -> listOf(
            Permission.EVAL, Permission.READ, Permission.WRITE, Permission.DELETE
        )
        Role.USER -> listOf(
            Permission.EVAL, Permission.READ, Permission.WRITE
        )
        Role.READER -> listOf(
            Permission.READ
        )
    }
