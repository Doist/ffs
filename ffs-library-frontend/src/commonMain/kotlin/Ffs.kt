
import doist.ffs.Client
import doist.ffs.DEFAULT_URL
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
    override val data: MutableMap<String, Boolean> = mutableMapOf()

    override fun updateData(response: String): Unit = data.putAll(Json.decodeFromString(response))

    override fun isEnabled(name: String): Boolean = data[name] ?: false
}
