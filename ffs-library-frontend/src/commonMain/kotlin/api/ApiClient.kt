package doist.ffs.api

import doist.ffs.ext.stream
import doist.ffs.sse.SseEvent
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.utils.io.core.Closeable
import kotlinx.coroutines.CoroutineScope

internal class ApiClient(
    private val url: String,
    private val httpClient: HttpClient
) : CoroutineScope by httpClient, Closeable by httpClient {
    constructor(url: String, engine: HttpClientEngine? = null) : this(
        url,
        if (engine != null) {
            HttpClient(engine, CONFIG_HTTP_ENGINE)
        } else {
            HttpClient(CONFIG_HTTP_ENGINE)
        }
    )

    suspend fun getFlagsEval(config: HttpRequestBuilder.() -> Unit): HttpResponse =
        httpClient.get("$url/flags/eval", config)

    suspend fun streamFlagsEval(
        config: HttpRequestBuilder.() -> Unit,
        block: suspend (SseEvent) -> Unit
    ) {
        httpClient.stream("$url/flags/eval", config, block)
    }

    companion object {
        private val CONFIG_HTTP_ENGINE: HttpClientConfig<*>.() -> Unit = {
            install(ContentNegotiation)
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.HEADERS
            }
        }
    }
}
