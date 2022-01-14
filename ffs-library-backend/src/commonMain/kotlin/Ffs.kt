package doist.ffs

import doist.ffs.model.Flag
import doist.ffs.serialization.json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.JsonObject

public class Ffs(
    apiToken: String,
    url: String = DEFAULT_URL,
    liveUpdates: Boolean = true
) : Client<Map<String, Flag>>(
    apiToken,
    url,
    "/flags",
    liveUpdates
) {
    protected override val data: MutableMap<String, Flag> = mutableMapOf()

    protected override fun updateData(response: String) {
        val flags: List<Flag> = json.decodeFromString(response)
        flags.forEach {
            data[it.name] = it
        }
    }

    public override fun isEnabled(name: String): Boolean = data[name]?.isEnabled(env) ?: false
}

private fun Flag.isEnabled(env: JsonObject): Boolean {
    if (archived_at != null) return false
    return doist.ffs.rule.isEnabled(rule, env, id)
}
