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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.collections.set

public class Ffs private constructor(private val config: Config) : Config by config {
    public constructor(
        apiToken: String,
        projectId: Long,
        url: String = "https://ffs.doist.com/",
        liveUpdates: Boolean = true
    ) : this(BaseConfig(apiToken, projectId, url, liveUpdates))

    private var client: ApiClient? = null

    // TODO: Store and read from storage.
    private val flags = mutableMapOf<String, Boolean>()

    public fun initialize() {
        initialize(null)
    }

    internal fun initialize(engine: HttpClientEngine?): Job {
        shutdown()

        ApiClient(config.url, engine).let {
            client = it
            return it.launch {
                val configRequest: HttpRequestBuilder.() -> Unit = {
                    accept(ContentType.Application.Json)
                    bearerAuth(config.apiToken)

                    parameter("project_id", config.projectId)
                    parameter("env", config.env)
                }

                if (config.liveUpdates) {
                    it.streamFlagsEval(configRequest) { (data, _, _) ->
                        flags.putAll(Json.decodeFromString<Map<String, Boolean>>(data))
                    }
                } else {
                    val response = it.getFlagsEval(configRequest)
                    flags.putAll(Json.decodeFromString(response.body()))
                }
            }
        }
    }

    public fun isEnabled(name: String): Boolean = flags[name] ?: false

    public fun shutdown() {
        if (client == null) return

        client.let {
            client = null
            it?.close()
        }
    }

    @Suppress("TooManyFunctions")
    private class BaseConfig(
        override val apiToken: String,
        override val projectId: Long,
        override val url: String,
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
