package doist.ffs

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Frontend library for FFS that synchronizes rules and their evaluations.
 */
public class Ffs(
    apiToken: String,
    url: String = DEFAULT_URL,
    liveUpdates: Boolean = DEFAULT_LIVE_UPDATES
) : Client<Map<String, Boolean>>(
    apiToken,
    url,
    "/flags/eval",
    liveUpdates
) {
    public constructor(apiToken: String, url: String) :
        this(apiToken, url, DEFAULT_LIVE_UPDATES)
    public constructor(apiToken: String, liveUpdates: Boolean) :
        this(apiToken, DEFAULT_URL, liveUpdates)

    protected override val data: MutableMap<String, Boolean> = mutableMapOf()

    protected override fun updateData(response: String): Unit =
        data.putAll(Json.decodeFromString(response))

    /**
     * @inheritDoc
     *
     * Flag evaluation is done on the server, which provides the results for all evaluations.
     */
    public override fun isEnabled(name: String, default: Boolean): Boolean = data[name] ?: default
}
