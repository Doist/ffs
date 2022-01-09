package doist.ffs.auth

sealed class Resource(open val id: Long) {
    /**
     * Organizations include themselves and related resources, such as projects and flags.
     */
    data class Organization(override val id: Long) : Resource(id)

    /**
     * Projects include themselves and related resources, such as flags.
     */
    class Project(override val id: Long) : Resource(id)
}
