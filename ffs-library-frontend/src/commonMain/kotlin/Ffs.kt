import doist.ffs.Client
import doist.ffs.DEFAULT_URL
import kotlinx.serialization.builtins.serializer

public class Ffs(
    apiToken: String,
    projectId: Long,
    url: String = DEFAULT_URL,
    liveUpdates: Boolean = true
) : Client<Boolean>(
    apiToken,
    projectId,
    url,
    "/flags/eval",
    liveUpdates,
    Boolean.serializer()
) {
    override fun isEnabled(name: String): Boolean = map[name] ?: false
}
