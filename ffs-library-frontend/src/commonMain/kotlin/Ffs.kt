package doist.ffs

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

public class Ffs(
    apiToken: String,
    url: String = DEFAULT_URL,
    liveUpdates: Boolean = true
) : Client<Map<String, Boolean>>(
    apiToken,
    url,
    "/flags/eval",
    liveUpdates
) {
    protected override val data: MutableMap<String, Boolean> = mutableMapOf()

    protected override fun updateData(response: String): Unit =
        data.putAll(Json.decodeFromString(response))

    public override fun isEnabled(name: String): Boolean = data[name] ?: false
}
