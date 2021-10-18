@file:Suppress("NOTHING_TO_INLINE")

package doist.ffs.routes

import doist.ffs.serialization.json
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.formUrlEncode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.contentType
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import kotlinx.serialization.decodeFromString

inline fun <reified T> TestApplicationEngine.assertResourceCount(
    path: String,
    size: Int
) {
    with(handleRequest(HttpMethod.Get, path)) {
        assert(response.status() == HttpStatusCode.OK)
        assert(response.contentType().match(ContentType.Application.Json))
        val resources = json.decodeFromString<List<T>>(response.content!!)
        assert(resources.size == size)
    }
}

inline fun TestApplicationEngine.assertResourceCreates(
    path: String,
    args: List<Pair<String, String?>>
) = with(
    handleRequest(HttpMethod.Post, path) {
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
    path: String,
    block: (T) -> Unit = {}
) = with(handleRequest(HttpMethod.Get, path)) {
    assert(response.status() == HttpStatusCode.OK)
    assert(response.contentType().match(ContentType.Application.Json))
    val resource = json.decodeFromString<T>(response.content!!)
    block(resource)
}

inline fun TestApplicationEngine.assertResourceUpdates(
    path: String,
    args: List<Pair<String, String?>>
) {
    with(
        handleRequest(HttpMethod.Put, path) {
            addHeader(
                HttpHeaders.ContentType,
                ContentType.Application.FormUrlEncoded.toString()
            )
            setBody(args.formUrlEncode())
        }
    ) {
        assert(response.status() == HttpStatusCode.NoContent)
    }
}

inline fun TestApplicationEngine.assertResourceDeletes(path: String) {
    with(handleRequest(HttpMethod.Delete, path)) {
        assert(response.status() == HttpStatusCode.NoContent)
    }
}
