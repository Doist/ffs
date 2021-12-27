package doist.ffs.api

import doist.ffs.ext.stream
import doist.ffs.sse.SseEvent
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.utils.io.core.Closeable
import kotlinx.coroutines.CoroutineScope

internal class ApiClient(
    private val url: String,
    private val path: String,
    private val httpClient: HttpClient
) : CoroutineScope by httpClient, Closeable by httpClient {
    constructor(url: String, path: String, engine: HttpClientEngine? = null) : this(
        url,
        path,
        if (engine != null) {
            HttpClient(engine, CONFIG_HTTP_ENGINE)
        } else {
            HttpClient(CONFIG_HTTP_ENGINE)
        }
    )

    suspend fun get(config: HttpRequestBuilder.() -> Unit): HttpResponse =
        httpClient.get("$url$path", config)

    suspend fun stream(
        config: HttpRequestBuilder.() -> Unit,
        block: suspend (SseEvent) -> Unit
    ) {
        httpClient.stream("$url$path", config, block)
    }

    companion object {
        private val CONFIG_HTTP_ENGINE: HttpClientConfig<*>.() -> Unit = {
            install(ContentNegotiation)
        }
    }
}
