@file:OptIn(ExperimentalSerializationApi::class)

package doist.ffs.serialization

import doist.ffs.db.Flag
import doist.ffs.db.Organization
import doist.ffs.db.Project
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

@Serializer(forClass = Organization::class)
private object OrganizationSerializer

@Serializer(forClass = Project::class)
private object ProjectSerializer

@Serializer(forClass = Flag::class)
private object FlagSerializer

val json = Json {
    isLenient = true
    serializersModule = SerializersModule {
        contextual(OrganizationSerializer)
        contextual(ProjectSerializer)
        contextual(FlagSerializer)
    }
}
