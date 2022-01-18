package doist.ffs.db

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val id: Long,
    @SerialName("organization_id")
    val organizationId: Long,
    val name: String
)
