package doist.ffs.serialization

import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.cacheControl
import io.ktor.server.response.respondTextWriter
import kotlinx.coroutines.channels.ReceiveChannel

internal const val SSE_FIELD_PREFIX_ID = "id: "
internal const val SSE_FIELD_PREFIX_EVENT = "event: "
internal const val SSE_FIELD_PREFIX_DATA = "data: "

internal data class SseEvent(val data: String, val event: String? = null, val id: String? = null)

/**
 * Reads [SseEvent] from the specified [ReceiveChannel] and serializes them in a way that is
 * compatible with the Server-Sent Events specification.
 */
@Suppress("BlockingMethodInNonBlockingContext")
internal suspend fun ApplicationCall.respondSse(
    status: HttpStatusCode,
    events: ReceiveChannel<SseEvent>
) {
    response.cacheControl(CacheControl.NoCache(null))
    respondTextWriter(ContentType.Text.EventStream, status) {
        for (event in events) {
            if (event.id != null) {
                write("$SSE_FIELD_PREFIX_ID${event.id}\n")
            }
            if (event.event != null) {
                write("$SSE_FIELD_PREFIX_EVENT${event.event}\n")
            }
            for (dataLine in event.data.lines()) {
                write("$SSE_FIELD_PREFIX_DATA$dataLine\n")
            }
            write("\n")
            flush()
        }
    }
}
