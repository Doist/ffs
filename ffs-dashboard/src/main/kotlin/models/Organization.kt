package doist.ffs.models

import kotlinx.serialization.Serializable

@Serializable
data class Organization(val id: Long, val name: String)
