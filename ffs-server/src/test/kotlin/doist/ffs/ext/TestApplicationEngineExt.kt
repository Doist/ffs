package doist.ffs.ext

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.util.pipeline.execute
import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield

/**
 * Make a test request that sets up an SSE session and invokes a [callback] function to
 * receive events from the server.
 */
@Suppress("EXPERIMENTAL_API_USAGE_FUTURE_ERROR")
fun TestApplicationEngine.handleSse(
    uri: String,
    setup: TestApplicationRequest.() -> Unit = {},
    callback: suspend TestApplicationCall.(incoming: ByteReadChannel) -> Unit
): TestApplicationCall {
    val call = createCall(closeRequest = false) {
        this.uri = uri
        addHeader(HttpHeaders.Accept, ContentType.Text.EventStream.toString())
        setup()
        bodyChannel = ByteChannel(true)
    }

    launch(call.coroutineContext) {
        // Execute server side.
        pipeline.execute(call)
    }

    runBlocking(call.coroutineContext) {
        // responseChannelDeferred is internal, so we wait like this.
        // Ref: https://github.com/ktorio/ktor/blob/c5877a22c91fd693ea6dcd0b4e1924f05d3b6825/ktor-server/ktor-server-test-host/jvm/src/io/ktor/server/testing/TestApplicationEngine.kt#L225-L230
        var responseChannel: ByteReadChannel?
        do {
            // Ensure status is absent or valid.
            val status = call.response.status()
            if (status?.isSuccess() == false) {
                throw IllegalStateException(status.toString())
            }

            // Suspend, then try to grab response channel.
            yield()
            // websocketChannel is just responseChannel internally.
            responseChannel = call.response.websocketChannel()
        } while (responseChannel == null)

        // Execute client side.
        call.callback(responseChannel)
    }

    return call
}
