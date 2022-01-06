@file:Suppress("NOTHING_TO_INLINE")

package doist.ffs.routes

import doist.ffs.serialization.json
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.auth.AuthScheme
import io.ktor.http.formUrlEncode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.contentType
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import kotlinx.serialization.decodeFromString

inline fun <reified T> TestApplicationEngine.assertResourceCount(
    uri: String,
    token: String? = null,
    count: Int
) = with(
    handleRequest(HttpMethod.Get, uri) {
        token?.let {
            addHeader(HttpHeaders.Authorization, "${AuthScheme.Bearer} $it")
        }
    }
) {
    assert(response.status() == HttpStatusCode.OK)
    assert(response.contentType().match(ContentType.Application.Json))
    val resources = json.decodeFromString<List<T>>(response.content!!)
    assert(resources.size == count)
}

inline fun TestApplicationEngine.assertResourceCreates(
    uri: String,
    token: String? = null,
    args: List<Pair<String, String?>> = emptyList(),
) = with(
    handleRequest(HttpMethod.Post, uri) {
        token?.let {
            addHeader(HttpHeaders.Authorization, "${AuthScheme.Bearer} $it")
        }
        addHeader(
            HttpHeaders.ContentType,
            ContentType.Application.FormUrlEncoded.toString()
        )
        setBody(args.formUrlEncode())
    }
) {
    assert(response.status() == HttpStatusCode.Created)
    response.headers[HttpHeaders.Location].also {
        assert(it != null)
    }
}!!

inline fun <reified T> TestApplicationEngine.assertResource(
    uri: String,
    method: HttpMethod = HttpMethod.Get,
    args: List<Pair<String, String?>>? = null,
    token: String? = null,
    block: (T) -> Unit = {},
) = with(
    handleRequest(method, uri) {
        token?.let {
            addHeader(HttpHeaders.Authorization, "${AuthScheme.Bearer} $it")
        }
        args?.let {
            addHeader(
                HttpHeaders.ContentType,
                ContentType.Application.FormUrlEncoded.toString()
            )
            setBody(args.formUrlEncode())
        }
    }
) {
    assert(response.status() == HttpStatusCode.OK)
    assert(response.contentType().match(ContentType.Application.Json))
    val resource = json.decodeFromString<T>(response.content!!)
    block(resource)
}

inline fun TestApplicationEngine.assertResourceUpdates(
    uri: String,
    args: List<Pair<String, String?>>? = null,
    token: String? = null,
) = assertStatus(
    uri = uri,
    method = HttpMethod.Put,
    args = args,
    token = token,
    status = HttpStatusCode.NoContent
)

inline fun TestApplicationEngine.assertResourceDeletes(
    uri: String,
    args: List<Pair<String, String?>>? = null,
    token: String? = null,
) = assertStatus(
    uri = uri,
    method = HttpMethod.Delete,
    args = args,
    token = token,
    status = HttpStatusCode.NoContent
)

inline fun TestApplicationEngine.assertStatus(
    uri: String,
    method: HttpMethod = HttpMethod.Get,
    args: List<Pair<String, String?>>? = null,
    token: String? = null,
    status: HttpStatusCode,
) = with(
    handleRequest(method, uri) {
        token?.let {
            addHeader(HttpHeaders.Authorization, "${AuthScheme.Bearer} $it")
        }
        args?.let {
            addHeader(
                HttpHeaders.ContentType,
                ContentType.Application.FormUrlEncoded.toString()
            )
            setBody(args.formUrlEncode())
        }
    }
) {
    assert(response.status() == status)
}
