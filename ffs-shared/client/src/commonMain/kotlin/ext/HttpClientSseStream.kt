package ext

import doist.ffs.sse.LastEventID
import doist.ffs.sse.SSE_DEFAULT_RETRY
import doist.ffs.sse.SSE_FIELD_PREFIX_DATA
import doist.ffs.sse.SSE_FIELD_PREFIX_EVENT
import doist.ffs.sse.SSE_FIELD_PREFIX_ID
import doist.ffs.sse.SSE_FIELD_PREFIX_RETRY
import doist.ffs.sse.SseEvent
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Suppress("ComplexMethod")
internal suspend fun HttpClient.stream(
    url: String,
    builder: HttpRequestBuilder.() -> Unit = {},
    block: suspend (SseEvent) -> Unit
) {
    var retry: Long = SSE_DEFAULT_RETRY
    var lastEventId: String? = null
    while (isActive) {
        try {
            prepareGet(url) {
                header(HttpHeaders.Accept, ContentType.Text.EventStream)
                header(HttpHeaders.CacheControl, CacheControl.NoCache(null))
                if (lastEventId != null) {
                    header(HttpHeaders.LastEventID, lastEventId)
                }
                builder()
            }.body<ByteReadChannel, Unit> { channel ->
                // Read SseEvents and emit invoke block until the connection stops being active.
                while (isActive) {
                    // Read lines until blank line is found, parsing an SseEvent from them.
                    var id: String? = null
                    var event: String? = null
                    val data = StringBuilder()

                    while (isActive) {
                        val line = channel.readUTF8Line()
                        when {
                            // Line is null, the channel has been closed. Retry by reconnecting.
                            line == null -> return@body

                            // Line is not blank, read it.
                            line.isNotBlank() -> {
                                val (key, value) = line.split(": ", limit = 2)
                                when ("$key: ") {
                                    SSE_FIELD_PREFIX_ID -> {
                                        id = value
                                        lastEventId = value
                                    }
                                    SSE_FIELD_PREFIX_EVENT -> event = value
                                    SSE_FIELD_PREFIX_DATA -> data.append(value)
                                    SSE_FIELD_PREFIX_RETRY -> retry = value.toLongOrNull() ?: retry
                                    else -> error("Unrecognized event-stream key $key")
                                }
                            }

                            // Line is blank, so send the event if complete.
                            // Break from inner loop to reinitialize the state and restart reading.
                            line.isBlank() -> {
                                if (data.isNotBlank()) {
                                    block(SseEvent(data.toString(), event, id))
                                }
                                break
                            }
                        }
                    }
                }
            }

            delay(retry)
        } catch (
            @Suppress("TooGenericExceptionCaught")
            throwable: Throwable
        ) {
            if (throwable is ResponseException || throwable is CancellationException) {
                // Stop retrying on network errors or cancellation.
                throw throwable
            } else {
                delay(retry)
            }
        }
    }
}
