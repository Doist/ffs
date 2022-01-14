package doist.ffs

import doist.ffs.model.Flag
import doist.ffs.serialization.json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.JsonObject

/**
 * Backend library for FFS that synchronizes rules and evaluates them locally.
 */
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

    /**
     * @inheritDoc
     *
     * Flag evaluation is done locally, after syncing the rules with the server.
     */
    public override fun isEnabled(name: String, default: Boolean): Boolean =
        data[name]?.isEnabled(env) ?: default
}

private fun Flag.isEnabled(env: JsonObject): Boolean? {
    if (archived_at != null) return null
    return doist.ffs.rule.isEnabled(rule, env, id)
}
