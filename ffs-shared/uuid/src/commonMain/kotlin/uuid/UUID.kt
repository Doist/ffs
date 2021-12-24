package doist.ffs.uuid

import kotlin.random.Random
import kotlin.random.nextUBytes

@Suppress("UnnecessaryAbstractClass", "MagicNumber", "ComplexCondition")
public abstract class UUID private constructor(private val random: Random = Random.Default) {
    constructor(seed: Int) : this(Random(seed))

    /**
     * From [RFC 4122](https://datatracker.ietf.org/doc/html/rfc4122), the UUID string
     * representation follows the structure:
     * UUID                   = time-low "-" time-mid "-"
     *                          time-high-and-version "-"
     *                          clock-seq-and-reserved
     *                          clock-seq-low "-" node
     * time-low               = 4hexOctet
     * time-mid               = 2hexOctet
     * time-high-and-version  = 2hexOctet
     * clock-seq-and-reserved = hexOctet
     * clock-seq-low          = hexOctet
     * node                   = 6hexOctet
     * hexOctet               = hexDigit hexDigit
     * hexDigit =
     *      "0" / "1" / "2" / "3" / "4" / "5" / "6" / "7" / "8" / "9" /
     *      "a" / "b" / "c" / "d" / "e" / "f" /
     *      "A" / "B" / "C" / "D" / "E" / "F"
     */
    fun generateV4(): String {
        val bytes = random.nextUBytes(16)
        // Set the version 4 (4 bits) in octet 6: 0100XXXX.
        bytes[6] = (bytes[6] and 0x0fu) or 0x40u
        // Set the variant 1 (2 bits) in octet 8: 10XXXXXX.
        bytes[8] = (bytes[8] and 0x3fu) or 0x80u
        return buildString {
            bytes.forEachIndexed { i, byte ->
                val byteStr = byte.toString(radix = 16)
                if (i == 4 || i == 6 || i == 8 || i == 10) {
                    append('-')
                }
                if (byteStr.length < 2) {
                    append('0')
                }
                append(byteStr)
            }
        }
    }

    companion object Default : UUID()
}
