@file:Suppress("ConstructorParameterNaming")

package doist.ffs.model

public data class Flag(
    public val id: Long,
    public val name: String,
    public val rule: String,
    public val archived_at: Long?
)
