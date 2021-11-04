@file:Suppress("BlockingMethodInNonBlockingContext")

package doist.ffs.serialization

import doist.ffs.db.Flag
import doist.ffs.routes.SSE_FIELD_PREFIX_DATA
import doist.ffs.routes.SSE_FIELD_PREFIX_ID
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.WriterContent
import io.ktor.serialization.ContentConverter
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.charsets.Charset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlin.reflect.typeOf

class FlowConverter : ContentConverter {
    override suspend fun deserialize(
        charset: Charset,
        typeInfo: TypeInfo,
        content: ByteReadChannel
    ) = throw NotImplementedError("FlowConverter can only serialize, not deserialize")

    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun serialize(
        contentType: ContentType,
        charset: Charset,
        typeInfo: TypeInfo,
        value: Any
    ) = when (val type = typeInfo.kotlinType) {
        typeOf<Flow<List<Flag>>>() -> {
            @Suppress("UNCHECKED_CAST")
            val flow = value as Flow<List<Flag>>
            WriterContent(
                {
                    var lastUpdatedFlags = emptyList<Flag>()
                    flow.collect { flags ->
                        // Select flags that changed since the last emission by picking those that
                        // were updated as/more recently as the most recent in the previous batch,
                        // and were not contained in the batch itself.
                        // Comparisons are safe, as instances are not shared.
                        val lastUpdatedAt =
                            lastUpdatedFlags.firstOrNull()?.updated_at ?: Instant.DISTANT_PAST
                        val updatedFlags = flags.filter { flag ->
                            flag.updated_at >= lastUpdatedAt && !lastUpdatedFlags.contains(flag)
                        }

                        // Write newly updated flags and last updated epoch.
                        write("$SSE_FIELD_PREFIX_DATA${json.encodeToString(updatedFlags)}\n")
                        write("$SSE_FIELD_PREFIX_ID${lastUpdatedAt.epochSeconds}\n")
                        write("\n")
                        flush()

                        // Retain most recently updated flags to exclude in next emission.
                        updatedFlags.maxOfOrNull { it.updated_at }?.let { updatedAt ->
                            lastUpdatedFlags = updatedFlags.filter { flag ->
                                flag.updated_at == updatedAt
                            }
                        }
                    }
                },
                contentType,
                HttpStatusCode.OK
            )
        }
        else -> throw NotImplementedError("Flow conversion for $type is not implemented")
    }
}
