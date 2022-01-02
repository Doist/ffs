
import doist.ffs.initializeInternal
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// This can and should be multiplatform, but building fails due to a coroutines version mismatch.
// See: https://youtrack.jetbrains.com/issue/KT-50222
class FfsTest {
    @Test
    fun testFlagEnabled() = runTest {
        val ffs = Ffs("apitoken", liveUpdates = false)
        assertFalse(ffs.isEnabled("test"))
        ffs.initializeInternal(
            MockEngine {
                respond(
                    content = ByteReadChannel("""{"test": true}"""),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        ).join()
        assertTrue(ffs.isEnabled("test"))
    }
}
