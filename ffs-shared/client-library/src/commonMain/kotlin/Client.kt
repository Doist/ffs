@file:Suppress("ForbiddenComment")

package doist.ffs

import doist.ffs.api.ApiClient
import doist.ffs.env.ENV_DEVICE_LOCALE
import doist.ffs.env.ENV_DEVICE_NAME
import doist.ffs.env.ENV_INTERNAL_ROLLOUT_ID
import doist.ffs.env.ENV_USER_EMAIL
import doist.ffs.env.ENV_USER_ID
import doist.ffs.uuid.UUID
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.collections.set

public const val DEFAULT_URL: String = "https://ffs.doist.com"

abstract class Client<T> private constructor(private val config: BaseConfig) : Config by config {
    constructor(
        apiToken: String,
        url: String,
        path: String,
        liveUpdates: Boolean
    ) : this(BaseConfig(apiToken, url, path, liveUpdates))

    private var apiClient: ApiClient? = null

    // TODO: Store and read from storage.
    // Could be immutable externally in Kotlin 1.7.0.
    // See: https://youtrack.jetbrains.com/issue/KT-14663
    protected abstract val data: T

    fun initialize() {
        initialize(null)
    }

    internal fun initialize(engine: HttpClientEngine?): Job {
        shutdown()

        ApiClient(config.url, config.path, engine).let {
            apiClient = it
            return it.launch {
                val configRequest: HttpRequestBuilder.() -> Unit = {
                    accept(ContentType.Application.Json)
                    bearerAuth(config.apiToken)

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
        }
    }

    protected abstract fun updateData(response: String): Unit

    abstract fun isEnabled(name: String): Boolean

    fun shutdown() {
        if (apiClient == null) return

        apiClient.let {
            apiClient = null
            it?.close()
        }
    }

    @Suppress("TooManyFunctions")
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

        override fun setDeviceLocale(locale: String): Config = apply {
            map[ENV_DEVICE_LOCALE] = JsonPrimitive(locale)
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
