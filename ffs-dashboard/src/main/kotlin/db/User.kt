package doist.ffs.db

import kotlinx.serialization.Serializable

@Serializable
data class User(val name: String, val email: String)
