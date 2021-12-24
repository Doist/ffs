package doist.ffs.ext

import doist.ffs.sse.HEADER_LAST_EVENT_ID
import doist.ffs.sse.SSE_FIELD_PREFIX_RETRY
import doist.ffs.sse.SseEvent
import io.ktor.client.HttpClient
import io.ktor.client.utils.EmptyContent.status
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.request.header
import io.ktor.server.response.cacheControl
import io.ktor.server.response.respondTextWriter
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import sse.write
import kotlin.test.Test
import kotlin.test.assertTrue
import io.ktor.client.engine.cio.CIO as CIOEngine

class SseIntegrationTest {
    @Test
    fun testSseIntegration() = runTest {
        var lastId = 0
        val batch = 5

        // Setup and start server.
        val port = 56473
        val server = embeddedServer(CIO, port) {
            routing {
                get("/") {
                    lastId = call.request.header(HEADER_LAST_EVENT_ID)?.toInt() ?: 0
                    val flow = flow {
                        repeat(batch) {
                            delay(50)
                            emit(lastId + it + 1)
                        }
                    }.map {
                        SseEvent(data = it.toString(), id = it.toString())
                    }
                    call.stream(flow, retry = 100)
                }
            }
        }
        server.start(wait = false)

        // Setup and start client.
        val client = HttpClient(CIOEngine)
        launch {
            client.stream("http://localhost:$port") {
            }
        }

        // Wait for streaming, and a retry.
        withContext(Dispatchers.Default) {
            delay(1500)
        }

        // Stop client and server.
        client.close()
        server.stop(500, 500)

        // Ensure that the last event id is correct.
        assertTrue(lastId >= 10)
        assertTrue(lastId % batch == 0)
    }

    private suspend fun ApplicationCall.stream(events: Flow<SseEvent>, retry: Int? = null) {
        response.cacheControl(CacheControl.NoCache(null))
        respondTextWriter(ContentType.Text.EventStream, status) {
            retry?.let {
                write("$SSE_FIELD_PREFIX_RETRY$it\n")
            }
            events.collect { event ->
                event.write(this)
            }
        }
    }
}
