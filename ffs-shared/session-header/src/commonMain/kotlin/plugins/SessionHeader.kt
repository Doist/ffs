package doist.ffs.plugins

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.HttpSendPipeline
import io.ktor.client.statement.HttpReceivePipeline
import io.ktor.client.statement.request
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.clone
import io.ktor.util.AttributeKey

/**
 * [HttpClient] plugin that handles session headers using a specific [name],
 * by automatically sending them after receiving them.
 *
 * Based on [HttpCookies].
 */
class SessionHeader internal constructor(
    private val name: String,
    private val storage: SessionStorage
) {
    /**
     * [SessionHeader] configuration.
     */
    class Configuraton {
        /**
         * Name of the session header.
         */
        var name = "session"

        /**
         * [SessionStorage] that will be used at this plugin.
         * By default, it just uses an in-memory string.
         */
        var storage: SessionStorage = object : SessionStorage {
            val sessions = mutableMapOf<String, String>()

            override fun get(url: Url): String? = sessions[url.host]

            override fun set(url: Url, value: String?) {
                if (value != null) {
                    sessions[url.host] = value
                } else {
                    sessions.remove(url.host)
                }
            }
        }
    }

    companion object : HttpClientPlugin<Configuraton, SessionHeader> {
        override val key: AttributeKey<SessionHeader> = AttributeKey("SessionHeader")

        override fun prepare(block: Configuraton.() -> Unit): SessionHeader =
            Configuraton().apply(block).let { config -> SessionHeader(config.name, config.storage) }

        override fun install(plugin: SessionHeader, scope: HttpClient) {
            scope.sendPipeline.intercept(HttpSendPipeline.State) {
                plugin.apply {
                    storage.get(context.url.clone().build())?.let {
                        context.headers[name] = it
                    }
                }
            }

            scope.receivePipeline.intercept(HttpReceivePipeline.State) { response ->
                plugin.apply {
                    response.headers[name]?.let {
                        storage.set(response.request.url, it)
                    }
                    if (
                        response.request.headers[name] != null &&
                        response.status == HttpStatusCode.Unauthorized
                    ) {
                        storage.set(response.request.url, null)
                    }
                }
            }
        }
    }
}

interface SessionStorage {
    fun get(url: Url): String?
    fun set(url: Url, value: String?)
}
