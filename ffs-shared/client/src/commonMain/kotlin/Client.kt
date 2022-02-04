@file:Suppress("ForbiddenComment")

package doist.ffs

import doist.ffs.api.ApiClient
import doist.ffs.endpoints.AuthScheme
import doist.ffs.env.ENV_DEVICE_IP
import doist.ffs.env.ENV_DEVICE_LOCALE
import doist.ffs.env.ENV_DEVICE_NAME
import doist.ffs.env.ENV_DEVICE_OS
import doist.ffs.env.ENV_INTERNAL_ROLLOUT_ID
import doist.ffs.env.ENV_USER_EMAIL
import doist.ffs.env.ENV_USER_ID
import doist.ffs.uuid.UUID
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.collections.set

const val DEFAULT_URL: String = "https://api.ffs.delivery/v1"
const val DEFAULT_LIVE_UPDATES: Boolean = true

abstract class Client<T> private constructor(private val config: BaseConfig) : Config by config {
    constructor(
        apiToken: String,
        url: String,
        path: String,
        liveUpdates: Boolean
    ) : this(BaseConfig(apiToken, url, path, liveUpdates))

    private var apiClient: ApiClient? = null

    // TODO: Store and read from storage.
    protected abstract val data: T

    protected abstract fun updateData(response: String)

    /**
     * Asynchronously initializes the client by loading cached data and connecting with the server.
     */
    open fun initialize(onInitialized: (() -> Unit)? = null) {
        initialize(onInitialized, null)
    }

    internal fun initialize(engine: HttpClientEngine? = null) = initialize(null, engine)

    internal fun initialize(
        onInitialized: (() -> Unit)? = null,
        engine: HttpClientEngine? = null
    ): Job {
        shutdown()

        ApiClient(config.url, config.path, engine).let {
            apiClient = it
            val job = it.launch {
                val configRequest: HttpRequestBuilder.() -> Unit = {
                    accept(ContentType.Application.Json)
                    header(HttpHeaders.Authorization, "${AuthScheme.Token} ${config.apiToken}")

                    parameter("env", config.env)
                }

                if (config.liveUpdates) {
                    it.stream(configRequest) { (response, _, _) ->
                        updateData(response)
                    }
                } else {
                    val response = it.get(configRequest)
                    updateData(response.body())
                }
            }

            onInitialized?.let {
                job.invokeOnCompletion {
                    onInitialized()
                }
            }

            return job
        }
    }

    /**
     * Returns true if flag named [name] exists and evaluates to true, false otherwise.
     */
    fun isEnabled(name: String): Boolean = isEnabled(name, false)

    /**
     * Returns true if flag named [name] evaluates to true, false if it evaluates to false,
     * and [default] if it does not exist or is archived.
     */
    abstract fun isEnabled(name: String, default: Boolean): Boolean

    /**
     * Returns all flag evaluations.
     */
    abstract fun all(): Map<String, Boolean>

    /**
     * Shuts down the client, freeing associated resources.
     */
    open fun shutdown() {
        if (apiClient == null) return

        apiClient.let {
            apiClient = null
            it?.close()
        }
    }

    private class BaseConfig(
        override val apiToken: String,
        override val url: String,
        val path: String,
        override val liveUpdates: Boolean
    ) : Config {
        private val map = mutableMapOf<String, JsonElement>()
        override val env: JsonObject
            get() = JsonObject(map)

        init {
            // TODO: Store and read from storage.
            setRolloutId(UUID.generateV4())
        }

        override fun setRolloutId(rolloutId: String): Config = apply {
            map[ENV_INTERNAL_ROLLOUT_ID] = JsonPrimitive(rolloutId)
        }

        override fun setUserId(id: String): Config = apply {
            map[ENV_USER_ID] = JsonPrimitive(id)
        }

        override fun setUserEmail(email: String): Config = apply {
            map[ENV_USER_EMAIL] = JsonPrimitive(email)
        }

        override fun setDeviceName(name: String): Config = apply {
            map[ENV_DEVICE_NAME] = JsonPrimitive(name)
        }

        override fun setDeviceOs(os: String): Config = apply {
            map[ENV_DEVICE_OS] = JsonPrimitive(os)
        }

        override fun setDeviceLocale(locale: String): Config = apply {
            map[ENV_DEVICE_LOCALE] = JsonPrimitive(locale)
        }

        override fun setDeviceIp(ip: String): Config = apply {
            map[ENV_DEVICE_IP] = JsonPrimitive(ip)
        }

        override fun putString(key: String, value: String): Config = apply {
            map[key] = JsonPrimitive(value)
        }

        override fun putNumber(key: String, value: Number): Config = apply {
            map[key] = JsonPrimitive(value)
        }

        override fun putBoolean(key: String, value: Boolean): Config = apply {
            map[key] = JsonPrimitive(value)
        }

        override fun putListString(key: String, values: List<String>): Config = apply {
            map[key] = JsonArray(values.map { JsonPrimitive(it) })
        }

        override fun putListNumber(key: String, values: List<Number>): Config = apply {
            map[key] = JsonArray(values.map { JsonPrimitive(it) })
        }

        override fun putListBoolean(key: String, values: List<Boolean>): Config = apply {
            map[key] = JsonArray(values.map { JsonPrimitive(it) })
        }
    }
}

// Expose `initialize(HttpClientEngine)` to other modules, without exposing it in the final API.
fun Client<*>.initializeInternal(engine: HttpClientEngine?) = this.initialize(engine)
