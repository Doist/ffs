package doist.ffs.serialization

import doist.ffs.model.Flag
import kotlinx.serialization.Serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

@Serializer(forClass = Flag::class)
private object FlagSerializer

private val modules = SerializersModule {
    contextual(FlagSerializer)
}

internal val json = Json {
    isLenient = true
    serializersModule = modules
}
