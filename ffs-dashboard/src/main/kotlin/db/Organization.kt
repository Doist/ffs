package db

import doist.ffs.db.Role
import kotlinx.serialization.Serializable

@Serializable
data class Organization(val id: Long, val name: String, val role: Role) {
    constructor(id: Long, name: String) : this(id, name, Role.ADMIN)
}
