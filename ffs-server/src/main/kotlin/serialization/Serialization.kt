package doist.ffs.serialization

import doist.ffs.db.Flag
import doist.ffs.db.Organization
import doist.ffs.db.Project
import doist.ffs.db.SelectById
import doist.ffs.db.SelectOrganizationByUser
import kotlinx.serialization.Serializer
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

@Serializer(forClass = Organization::class)
private object OrganizationSerializer

@Serializer(forClass = SelectOrganizationByUser::class)
private object OrganizationSelectByUserSerializer

@Serializer(forClass = Project::class)
private object ProjectSerializer

@Serializer(forClass = Flag::class)
private object FlagSerializer

@Serializer(forClass = SelectById::class)
private object UserSelectByIdSerializer

private val modules = SerializersModule {
    contextual(OrganizationSerializer)
    contextual(OrganizationSelectByUserSerializer)
    contextual(ProjectSerializer)
    contextual(FlagSerializer)
    contextual(UserSelectByIdSerializer)
}

val json = Json {
    isLenient = true
    serializersModule = modules
}

val cbor = Cbor {
    serializersModule = modules
}
