
import doist.ffs.Ffs
import doist.ffs.initializeInternal
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FfsTest {
    @Test
    fun isEnabled() = runTest {
        val ffs = Ffs("apitoken", liveUpdates = false)
        assertFalse(ffs.isEnabled("test"))
        assertTrue(ffs.isEnabled("test", true))
        ffs.initializeForTesting()
        assertTrue(ffs.isEnabled("test"))
        assertTrue(ffs.isEnabled("test", false))
    }

    @Test
    fun all() = runTest {
        val ffs = Ffs("apitoken", liveUpdates = false)
        assertEquals(emptyMap(), ffs.all())
        ffs.initializeForTesting()
        assertEquals(mapOf("test" to true), ffs.all())
        assertTrue(ffs.isEnabled("test"))
    }

    private suspend fun Ffs.initializeForTesting() {
        initializeInternal(
            MockEngine {
                respond(
                    content = ByteReadChannel(
                        """[{"id":1,"name":"test","rule":"1","archived_at":null}]""".toByteArray()
                    ),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        ).join()
    }
}
