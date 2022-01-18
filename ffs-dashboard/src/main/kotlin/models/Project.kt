package doist.ffs.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val id: Long,
    val name: String,
    @SerialName("organization_id")
    val organizationId: Long
)
