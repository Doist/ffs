package doist.ffs.rule

import com.goncalossilva.murmurhash.MurmurHash3
import doist.ffs.env.ENV_INTERNAL_ROLLOUT_ID
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private val murmurHash = MurmurHash3()

private val JsonObject.rolloutId: String?
    get() = this[ENV_INTERNAL_ROLLOUT_ID]?.jsonPrimitive?.contentOrNull

/**
 * Returns true when the given [formula] evaluates positively, false when it doesn't.
 *
 * The formula can be probabilistic, where it is true for only a subset of environments.
 *
 * @param formula the formula to parse.
 * @param env the environment map. Accepted values are booleans, numbers, strings, or lists of them.
 * @param flagId the flag id, paired with the environment's rollout id, used to determine a stable
 *               key for partial frequencies, ensuring that the formula is evaluated consistently
 *               between calls and across environments.
 */
@Suppress("MagicNumber")
fun isEnabled(formula: String, env: JsonObject, flagId: Any): Boolean {
    val key = env.rolloutId.takeUnless { it.isNullOrEmpty() }?.let { "$flagId$it" }
    return when (val probability = eval(formula, env)) {
        0f -> false
        1f -> true
        else -> when (key) {
            null -> false
            else -> {
                val hash = murmurHash.hash32x86(key.encodeToByteArray())
                hash % 100u < (probability * 100).toUInt()
            }
        }
    }
}
