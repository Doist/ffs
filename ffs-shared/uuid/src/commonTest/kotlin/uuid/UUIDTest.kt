package doist.ffs.uuid

import kotlin.test.Test
import kotlin.test.assertEquals

internal class UUIDTest {
    private val testUUID = object : UUID(seed = 123) { }

    @Test
    fun generateV4() {
        val uuids = List(100) { testUUID.generateV4() }
        uuids.forEach { uuid ->
            assertEquals(4, uuid.count { it == '-' })
            val octets = uuid.replace("-", "").chunked(2)
            assertEquals(16, octets.size)
            assertEquals(0x40u, octets[6].toUByte(radix = 16) and 0x40u)
            assertEquals(0x80u, octets[8].toUByte(radix = 16) and 0x80u)
        }
    }
}
