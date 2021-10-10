package doist.ffs.routes

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.formUrlEncode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.contentType
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

fun <T> TestApplicationEngine.assertResourceCount(
    path: String,
    deserializer: KSerializer<T>,
    size: Int
) {
    with(handleRequest(HttpMethod.Get, path)) {
        assert(response.status() == HttpStatusCode.OK)
        assert(response.contentType().match(ContentType.Application.Json))
        val organizations = Json.decodeFromString(ListSerializer(deserializer), response.content!!)
        assert(organizations.size == size)
    }
}

fun TestApplicationEngine.assertResourceCreates(
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

fun <T> TestApplicationEngine.assertResourceAtPath(
    path: String,
    deserializer: KSerializer<T>
) = with(handleRequest(HttpMethod.Get, path)) {
    assert(response.status() == HttpStatusCode.OK)
    assert(response.contentType().match(ContentType.Application.Json))
    Json.decodeFromString(deserializer, response.content!!)
}

fun TestApplicationEngine.assertResourceUpdates(path: String, args: List<Pair<String, String?>>) {
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

fun TestApplicationEngine.assertResourceDeletes(path: String) {
    with(handleRequest(HttpMethod.Delete, path)) {
        assert(response.status() == io.ktor.http.HttpStatusCode.NoContent)
    }
}
