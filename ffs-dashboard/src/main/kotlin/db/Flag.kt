package doist.ffs.db

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Flag(
    val id: Long,
    @SerialName("project_id")
    val projectId: Long,
    val name: String,
    val rule: String,
    @SerialName("archived_at")
    val archivedAt: Long?
)
