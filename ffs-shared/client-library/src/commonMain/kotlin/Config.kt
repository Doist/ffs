package doist.ffs

import kotlinx.serialization.json.JsonObject

@Suppress("TooManyFunctions")
interface Config {
    val apiToken: String

    val url: String
    val liveUpdates: Boolean

    val env: JsonObject

    fun setRolloutId(rolloutId: String): Config

    fun setUserId(id: String): Config
    fun setUserEmail(email: String): Config

    fun setDeviceName(name: String): Config
    fun setDeviceOs(name: String): Config
    fun setDeviceLocale(locale: String): Config

    fun putString(key: String, value: String): Config
    fun putNumber(key: String, value: Number): Config
    fun putBoolean(key: String, value: Boolean): Config

    fun putListString(key: String, values: List<String>): Config
    fun putListNumber(key: String, values: List<Number>): Config
    fun putListBoolean(key: String, values: List<Boolean>): Config
}
