package doist.ffs.ext

import doist.ffs.sse.SseEvent
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.cacheControl
import io.ktor.server.response.respondTextWriter
import kotlinx.coroutines.channels.ReceiveChannel
import sse.write

/**
 * Reads [SseEvent] events from the specified [ReceiveChannel] and serializes them
 * in a way compatible with the Server-Sent Events specification.
 */
@Suppress("BlockingMethodInNonBlockingContext")
internal suspend fun ApplicationCall.stream(
    status: HttpStatusCode,
    events: ReceiveChannel<SseEvent>
) {
    response.cacheControl(CacheControl.NoCache(null))
    respondTextWriter(ContentType.Text.EventStream, status) {
        for (event in events) {
            event.write(this)
        }
    }
}
