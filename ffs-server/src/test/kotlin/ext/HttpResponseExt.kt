package doist.ffs.ext

import doist.ffs.serialization.json
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.serialization.json.decodeFromStream

suspend inline fun <reified T> HttpResponse.bodyAsJson(): T =
    json.decodeFromStream(bodyAsChannel().toInputStream())
