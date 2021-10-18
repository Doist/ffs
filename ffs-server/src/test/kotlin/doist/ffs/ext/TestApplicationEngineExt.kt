import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.util.pipeline.execute
import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.Dispatchers
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
        addHeader(
            HttpHeaders.Accept,
            ContentType.Text.EventStream.toString()
        )
        setup()
        bodyChannel = ByteChannel(true)
    }

    launch(Dispatchers.IO) {
        // Execute server side.
        pipeline.execute(call)
    }

    runBlocking(Dispatchers.IO) {
        // websocketChannel is just responseChannel internally.
        var responseChannel = call.response.websocketChannel()
        // responseChannelDeferred is internal, so we wait like this.
        // Ref: https://github.com/ktorio/ktor/blob/c5877a22c91fd693ea6dcd0b4e1924f05d3b6825/ktor-server/ktor-server-test-host/jvm/src/io/ktor/server/testing/TestApplicationEngine.kt#L225-L230
        while (responseChannel == null) {
            yield()
            responseChannel = call.response.websocketChannel()
        }

        // Execute client side.
        call.callback(responseChannel)
    }

    return call
}
