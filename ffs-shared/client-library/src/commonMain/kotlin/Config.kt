package doist.ffs

import kotlinx.serialization.json.JsonObject

@Suppress("TooManyFunctions")
public interface Config {
    public val apiToken: String

    public val url: String
    public val liveUpdates: Boolean

    public val env: JsonObject

    public fun setRolloutId(rolloutId: String): Config

    public fun setUserId(id: String): Config
    public fun setUserEmail(email: String): Config

    public fun setDeviceName(name: String): Config
    public fun setDeviceLocale(locale: String): Config

    public fun putString(key: String, value: String): Config
    public fun putNumber(key: String, value: Number): Config
    public fun putBoolean(key: String, value: Boolean): Config

    public fun putListString(key: String, values: List<String>): Config
    public fun putListNumber(key: String, values: List<Number>): Config
    public fun putListBoolean(key: String, values: List<Boolean>): Config
}
