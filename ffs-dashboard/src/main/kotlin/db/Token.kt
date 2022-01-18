package doist.ffs.db

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Token(
    val id: Long,
    @SerialName("project_id")
    val projectId: Long,
    val description: String,
    @Transient
    val token: String? = null
)
