package doist.ffs.models

import kotlinx.serialization.Serializable

@Serializable
data class User(val name: String, val email: String)
