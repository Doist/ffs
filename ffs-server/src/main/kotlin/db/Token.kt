@file:Suppress("MatchingDeclarationName", "MagicNumber", "ReturnCount")

package doist.ffs.db

import java.util.zip.CRC32
import kotlin.random.Random
import kotlin.random.nextULong

private val Permission.tokenChar: Char
    get() = when (this) {
        Permission.READ -> 'r'
        Permission.EVAL -> 'x'
        else -> throw IllegalArgumentException("No tokens for permission $this")
    }

/**
 * Returns the [Permission] for [token], or `null` if none is applicable.
 */
fun Permission.Companion.fromToken(token: String) = Permission.values().firstOrNull {
    token.startsWith("ffs${it.tokenChar}_")
} ?: throw IllegalArgumentException("Invalid token: $token")

object TokenGenerator {
    /**
     * Generates a token, composed of 3 parts:
     * - A clearly identifiable prefix, that also conveys the token scope (read/eval).
     * - A random string, to avoid collisions.
     * - A checksum, to reduce false positives when identifying leaked tokens.
     *
     * Entropy is log2(36/log2(2)) * 48 = ~248 bits. For reference, GitHub tokens have ~178 bits.
     */
    fun generate(permission: Permission) = buildString {
        // Prefix with token scope (5 characters).
        append("ffs")
        append(permission.tokenChar)
        append("_")

        // Add randomness (48 characters).
        repeat(4) {
            append(Random.nextULong().toString(36).take(12).padStart(12, '0'))
        }
        // Suffix with checksum (7 characters).
        val crc = CRC32()
        crc.update(toString().encodeToByteArray())
        append(crc.value.toUInt().toString(36).padStart(7, '0'))
    }

    /**
     * Verifies if [token] follows the token format.
     */
    fun isFormatValid(token: String): Boolean {
        if (token.length != 60) return false
        val regex = Regex("(ffs[rx]_[0-9a-z]{48})([0-9a-z]{7})")
        val match = regex.matchEntire(token) ?: return false
        val (value, crc) = match.destructured
        return crc.toUInt(36) == CRC32().apply { update(value.encodeToByteArray()) }.value.toUInt()
    }
}
