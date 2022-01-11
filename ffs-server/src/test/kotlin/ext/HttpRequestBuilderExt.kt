package doist.ffs.ext

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.formUrlEncode

fun HttpRequestBuilder.setBodyForm(vararg parameters: Pair<String, Any>) {
    contentType(ContentType.Application.FormUrlEncoded)
    setBody(parameters.map { (key, value) -> key to value.toString() }.formUrlEncode())
}
