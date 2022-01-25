package doist.ffs.auth

import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import java.security.SecureRandom
import java.util.Base64

object Argon2Password {
    private const val SALT_LENGTH = 16
    private const val HASH_LENGTH = 32

    private val random = SecureRandom()

    private val base64Encoder = Base64.getEncoder().withoutPadding()
    private val base64Decoder = Base64.getDecoder()

    /**
     * Encode the raw password using Argon2id with default parameters.
     */
    fun encode(raw: String): String {
        val salt = ByteArray(SALT_LENGTH).also { random.nextBytes(it) }
        val hash = ByteArray(HASH_LENGTH)
        val params = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id).withSalt(salt).build()
        val generator = Argon2BytesGenerator()
        generator.init(params)
        generator.generateBytes(raw.encodeToByteArray(), hash)
        return encode(hash, params)
    }

    /**
     * Verify that the encoded password matches the raw password when it too is encoded.
     */
    fun matches(raw: String, encoded: String): Boolean {
        val decoded = runCatching { decode(encoded) }.getOrNull() ?: return false
        val hash = ByteArray(decoded.first.size)
        val generator = Argon2BytesGenerator()
        generator.init(decoded.second)
        generator.generateBytes(raw.encodeToByteArray(), hash)
        return decoded.first.contentEquals(hash)
    }

    /**
     * Encode the standard Argon2 hash-string as in the reference implementation:
     * https://github.com/P-H-C/phc-winner-argon2/blob/f57e61e19229e23c4445b85494dbf7c07de721cb/src/encoding.c#L244
     *
     * Paraphrasing:
     *
     * $argon2<T>[$v=<num>]$m=<num>,t=<num>,p=<num>$<bin>$<bin>
     *
     * where <T> is either 'd', 'id', or 'i', <num> is a decimal integer (positive,
     * fits in an 'unsigned long'), and <bin> is Base64-encoded data (no '=' padding
     * characters, no newline or whitespace).
     *
     * The last two binary chunks (encoded in Base64) are, in that order,
     * the salt and the output. Both are required. The binary salt length and the
     * output length must be in the allowed ranges defined in argon2.h.
     *
     * The ctx struct must contain buffers large enough to hold the salt and pwd
     * when it is fed into decode_string.
     */
    private fun encode(hash: ByteArray, params: Argon2Parameters) = buildString {
        when (params.type) {
            Argon2Parameters.ARGON2_d -> append("\$argon2d")
            Argon2Parameters.ARGON2_i -> append("\$argon2i")
            Argon2Parameters.ARGON2_id -> append("\$argon2id")
            else -> throw IllegalArgumentException("Unknown type: ${params.type}")
        }
        append("\$v=").append(params.version)
        append("\$m=").append(params.memory)
        append(",t=").append(params.iterations)
        append(",p=").append(params.lanes)
        if (params.salt != null) {
            append("$").append(base64Encoder.encodeToString(params.salt))
        }
        append("$").append(base64Encoder.encodeToString(hash))
    }

    /**
     * Decode the standard Argon2 hash-string as in the reference implementation:
     * https://github.com/P-H-C/phc-winner-argon2/blob/f57e61e19229e23c4445b85494dbf7c07de721cb/src/encoding.c#L244
     *
     * Paraphrasing:
     *
     * $argon2<T>[$v=<num>]$m=<num>,t=<num>,p=<num>$<bin>$<bin>
     *
     * where <T> is either 'd', 'id', or 'i', <num> is a decimal integer (positive,
     * fits in an 'unsigned long'), and <bin> is Base64-encoded data (no '=' padding
     * characters, no newline or whitespace).
     *
     * The last two binary chunks (encoded in Base64) are, in that order,
     * the salt and the output. Both are required. The binary salt length and the
     * output length must be in the allowed ranges defined in argon2.h.
     *
     * The ctx struct must contain buffers large enough to hold the salt and pwd
     * when it is fed into decode_string.
     */
    private fun decode(encodedHash: String): Pair<ByteArray, Argon2Parameters> {
        val builder: Argon2Parameters.Builder
        val parts = encodedHash.split("\$")
        require(parts.size >= 4) { "Invalid hash-string structure" }
        var currentPart = 1
        builder = when (parts[currentPart++]) {
            "argon2d" -> Argon2Parameters.Builder(Argon2Parameters.ARGON2_d)
            "argon2i" -> Argon2Parameters.Builder(Argon2Parameters.ARGON2_i)
            "argon2id" -> Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            else -> throw IllegalArgumentException("Unknown type: ${parts[0]}")
        }
        if (parts[currentPart].startsWith("v=")) {
            builder.withVersion(parts[currentPart++].substring(2).toInt())
        }
        val performanceParams = parts[currentPart++].split(",")
        require(performanceParams.size == 3) { "Invalid amount of performance parameters" }
        require(performanceParams[0].startsWith("m=")) { "Missing memory parameter" }
        builder.withMemoryAsKB(performanceParams[0].substring(2).toInt())
        require(performanceParams[1].startsWith("t=")) { "Missing iterations parameter" }
        builder.withIterations(performanceParams[1].substring(2).toInt())
        require(performanceParams[2].startsWith("p=")) { "Missing parallelism parameter" }
        builder.withParallelism(performanceParams[2].substring(2).toInt())
        builder.withSalt(base64Decoder.decode(parts[currentPart++]))
        return Pair(base64Decoder.decode(parts[currentPart]), builder.build())
    }
}
