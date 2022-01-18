package doist.ffs.plugins

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.statement.request
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

// This can and should be multiplatform, but building fails due to a coroutines version mismatch.
// See: https://youtrack.jetbrains.com/issue/KT-50222
class SessionHeaderTest {
    private val engine = MockEngine { data ->
        if (data.url.toString().endsWith("/auth")) {
            this.respond(
                content = "",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.Authorization, "Test 123")
            )
        } else if (data.url.toString().endsWith("/forbidden")) {
            this.respond(
                content = "",
                status = HttpStatusCode.Forbidden
            )
        } else if (data.url.toString().endsWith("/unauthorized")) {
            this.respond(
                content = "",
                status = HttpStatusCode.Unauthorized
            )
        } else {
            this.respond(
                content = "",
                status = HttpStatusCode.OK
            )
        }
    }

    @Test
    fun doesntIncludeHeaderIfNotReceived() = runTest {
        val client = HttpClient(engine) {
            install(SessionHeader) {
                name = HttpHeaders.Authorization
            }
        }

        client.get("http://example.com/ok").let { response ->
            assertTrue(response.request.headers[HttpHeaders.Authorization] == null)
        }

        client.get("http://example.com/ok").let { response ->
            assertTrue(response.request.headers[HttpHeaders.Authorization] == null)
        }
    }

    @Test
    fun includesHeaderIfReceived() = runTest {
        val client = HttpClient(engine) {
            install(SessionHeader) {
                name = HttpHeaders.Authorization
            }
        }

        client.get("http://example.com/auth").let { response ->
            assertTrue(response.request.headers[HttpHeaders.Authorization] == null)
        }

        client.get("http://example.com/ok").let { response ->
            assertTrue(response.request.headers[HttpHeaders.Authorization] == "Test 123")
        }
    }

    @Test
    fun clearsHeaderIfUnauthorized() = runTest {
        val client = HttpClient(engine) {
            expectSuccess = false
            install(SessionHeader) {
                name = HttpHeaders.Authorization
            }
        }

        client.get("http://example.com/auth").let { response ->
            assertTrue(response.request.headers[HttpHeaders.Authorization] == null)
        }

        client.get("http://example.com/unauthorized").let { response ->
            assertTrue(response.request.headers[HttpHeaders.Authorization] == "Test 123")
        }

        client.get("http://example.com/ok").let { response ->
            assertTrue(response.request.headers[HttpHeaders.Authorization] == null)
        }
    }

    @Test
    fun retainsHeaderIfForbidden() = runTest {
        val client = HttpClient(engine) {
            expectSuccess = false
            install(SessionHeader) {
                name = HttpHeaders.Authorization
            }
        }

        client.get("http://example.com/auth").let { response ->
            assertTrue(response.request.headers[HttpHeaders.Authorization] == null)
        }

        client.get("http://example.com/forbidden").let { response ->
            assertTrue(response.request.headers[HttpHeaders.Authorization] == "Test 123")
        }

        client.get("http://example.com/ok").let { response ->
            assertTrue(response.request.headers[HttpHeaders.Authorization] == "Test 123")
        }
    }
}
