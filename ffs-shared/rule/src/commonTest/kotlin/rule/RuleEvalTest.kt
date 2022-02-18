package doist.ffs.rule

import com.ionspin.kotlin.bignum.integer.toBigInteger
import doist.ffs.env.ENV_INTERNAL_ROLLOUT_ID
import kotlinx.datetime.Clock
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RuleEvalTest {
    private val baseEnv = JsonObject(mapOf(ENV_INTERNAL_ROLLOUT_ID to JsonPrimitive("rollout-id")))

    @Test
    fun enabled() {
        assertEquals(true, isEnabled("1", baseEnv, ""))
        assertEquals(false, isEnabled("0", baseEnv, ""))
        assertEquals(false, isEnabled("0.6", baseEnv, ""))
        assertEquals(true, isEnabled("0.7", baseEnv, ""))
    }

    @Test
    fun enabledDistribution() {
        val samples = 5000
        val distributions = arrayOf(0.2, 0.5, 0.9)
        for (distribution in distributions) {
            val actualCount = (1..samples).count {
                (1..Random.nextInt(10, 40))
                    .map { Random.nextInt(0, Char.MAX_VALUE.code).toChar() }
                    .joinToString("")
                    .let { isEnabled(distribution.toString(), baseEnv, it) }
            }
            val expectedCount = (samples * distribution).toInt()
            val tolerance = samples / 10
            assertTrue(actualCount in expectedCount - tolerance..expectedCount + tolerance)
        }
    }

    @Test
    fun booleans() {
        assertEquals(1f, eval("true"))
        assertEquals(0f, eval("false"))
    }

    @Test
    fun numbers() {
        assertEquals(0f, eval("0"))
        assertEquals(0.14159265f, eval("0.14159265"), 0.00001f)
        assertEquals(0.5f, eval("0.5"))
        assertEquals(1f, eval("1"))
        assertEquals(-1f, eval("-1"))
    }

    @Test
    fun strings() {
        assertEquals(0f, eval("\"abc\""))
        assertEquals(0.5f, eval("\"0.5\""))
        assertEquals(1f, eval("\"1\""))
    }

    @Test
    fun env() {
        assertEquals(0f, eval("""env["n"]"""))
        assertEquals(
            1f,
            eval("""isblank(env["n"])""", buildJsonObject { put("n", null as String?) })
        )

        assertEquals(0f, eval("""env["b"]""", buildJsonObject { put("b", false) }))
        assertEquals(1f, eval("""env["b"]""", buildJsonObject { put("b", true) }))

        assertEquals(0f, eval("""env["n"]""", buildJsonObject { put("n", 0f) }))
        assertEquals(0.5f, eval("""env["n"]""", buildJsonObject { put("n", 0.5f) }))
        assertEquals(1f, eval("""env["n"]""", buildJsonObject { put("n", 1f) }))

        assertEquals(0f, eval("""env["s"]""", buildJsonObject { put("s", "0") }))
        assertEquals(0.5f, eval("""env["s"]""", buildJsonObject { put("s", "0.5") }))
        assertEquals(1f, eval("""env["s"]""", buildJsonObject { put("s", "1") }))

        assertEquals(
            0f,
            eval("""contains("d", env["l"])""", buildJsonObject { put("l", listOf("a", "b", "c")) })
        )
        assertEquals(
            1f,
            eval("""contains("b", env["l"])""", buildJsonObject { put("l", listOf("a", "b", "c")) })
        )

        assertEquals(
            1f,
            eval(
                """isblank(env["i"])""",
                buildJsonObject {
                    put(
                        "i",
                        buildJsonObject {
                            put("k", "v")
                        }
                    )
                }
            )
        )
        assertEquals(
            1f,
            eval(
                """isblank(env["i"])""",
                buildJsonObject {
                    put(
                        "i",
                        buildJsonArray {
                            add(
                                buildJsonArray {
                                    add("a")
                                    add("b")
                                }
                            )
                        }
                    )
                }
            )
        )
    }

    @Test
    fun info() {
        assertEquals(1f, eval("""isblank("")"""))
        assertEquals(0f, eval("""isblank("notblank")"""))
    }

    @Test
    fun operators() {
        assertEquals(0f, eval("""eq(1, 0)"""))
        assertEquals(1f, eval("""eq(0, 0)"""))
        assertEquals(0f, eval("""eq(1, 0)"""))
        assertEquals(0f, eval("""eq("1", "0")"""))
        assertEquals(1f, eval("""eq("0", "0")"""))
        assertEquals(0f, eval("""eq("0", "1")"""))
        assertEquals(0f, eval("""eq([0, 1], [1, 0])"""))
        assertEquals(1f, eval("""eq([0, 1], [0, 1])"""))
        assertEquals(1f, eval("""eq(["0", "1"], ["0", "1"])"""))

        assertEquals(1f, eval("""gt(1, 0)"""))
        assertEquals(0f, eval("""gt(0, 0)"""))
        assertEquals(0f, eval("""gt(0, 1)"""))
        assertEquals(1f, eval("""gt("1", "0")"""))
        assertEquals(0f, eval("""gt("0", "0")"""))
        assertEquals(0f, eval("""gt("0", "1")"""))

        assertEquals(1f, eval("""gte(1, 0)"""))
        assertEquals(1f, eval("""gte(0, 0)"""))
        assertEquals(0f, eval("""gte(0, 1)"""))
        assertEquals(1f, eval("""gte("1", "0")"""))
        assertEquals(1f, eval("""gte("0", "0")"""))
        assertEquals(0f, eval("""gte("0", "1")"""))

        assertEquals(0f, eval("""lt(1, 0)"""))
        assertEquals(0f, eval("""lt(0, 0)"""))
        assertEquals(1f, eval("""lt(0, 1)"""))
        assertEquals(0f, eval("""lt("1", "0")"""))
        assertEquals(0f, eval("""lt("0", "0")"""))
        assertEquals(1f, eval("""lt("0", "1")"""))

        assertEquals(0f, eval("""lte(1, 0)"""))
        assertEquals(1f, eval("""lte(0, 0)"""))
        assertEquals(1f, eval("""lte(0, 1)"""))
        assertEquals(0f, eval("""lte("1", "0")"""))
        assertEquals(1f, eval("""lte("0", "0")"""))
        assertEquals(1f, eval("""lte("0", "1")"""))

        assertFailsWith<IllegalArgumentException> { eval("""gt(1)""") }
        assertFailsWith<IllegalArgumentException> { eval("""gte(1, 2, 3)""") }
        assertFailsWith<IllegalArgumentException> { eval("""lt([1, 2], [3, 4])""") }
        assertFailsWith<IllegalArgumentException> { eval("""lte(["1", "2"], ["3", "4"])""") }
    }

    @Test
    fun dates() {
        val now = Clock.System.now().epochSeconds.toFloat()
        assertTrue(eval("""now()""") in now..now + 1)

        assertEquals(1275430780f, eval("""datetime("2010-06-01T22:19:44Z")"""), 4f)
        assertEquals(1275394820f, eval("""datetime("2010-06-01T22:19:44+10:00")"""), 4f)
        assertEquals(1275430780f, eval("""datetime("2010-06-01T22:19:44")"""), 4f)
        assertEquals(1275350400f, eval("""datetime("2010-06-01")"""))

        assertFailsWith<IllegalArgumentException> {
            eval("""datetime("2010-06-01", "2010-06-01")""")
        }
        assertFailsWith<IllegalArgumentException> { eval("""datetime("20211022T154439Z")""") }
        assertFailsWith<IllegalArgumentException> { eval("""datetime("2021-W42")""") }
        assertFailsWith<IllegalArgumentException> { eval("""datetime("2021")""") }
        assertFailsWith<IllegalArgumentException> { eval("""datetime("22:19:44")""") }
        assertFailsWith<IllegalArgumentException> { eval("""datetime(2021)""") }
    }

    @Test
    fun text() {
        assertEquals(0f, eval("""matches("test@test.com", ".+@test.test")"""))
        assertEquals(0f, eval("""matches("test@com.test", ".+@test.test")"""))
        assertEquals(1f, eval("""matches("test@test.test", ".+@test.test")"""))

        assertFailsWith<IllegalArgumentException> { eval("""matches("1", "1", "1")""") }
        assertFailsWith<IllegalArgumentException> { eval("""matches(2, 1)""") }
        assertFailsWith<IllegalArgumentException> { eval("""matches(2, "1")""") }
        assertFailsWith<IllegalArgumentException> { eval("""matches("2", 1)""") }
        assertFailsWith<IllegalArgumentException> { eval("""matches(1, [1, 2])""") }
    }

    @Test
    fun arrays() {
        assertEquals(0f, eval("""contains("+00:00", ["+01:00", "+02:00"])"""))
        assertEquals(1f, eval("""contains("+01:00", ["+01:00", "+02:00"])"""))

        assertEquals(1f, eval("""contains(1, [1, 2])"""))
        assertEquals(0f, eval("""contains(3, [1, 2])"""))

        assertFailsWith<IllegalArgumentException> { eval("""contains("+01:00")""") }
        assertFailsWith<IllegalArgumentException> { eval("""contains("+01:00", "+01:00")""") }
        assertFailsWith<IllegalArgumentException> { eval("""contains("+01:00", "+01:00")""") }
    }

    @Test
    fun range() {
        assertEquals(0f, eval("""contains(11, [0:10])"""))
        assertEquals(0f, eval("""contains(15, [0:10])"""))
        assertEquals(0f, eval("""contains(99, [100:500])"""))
        assertEquals(0f, eval("""contains(-51, [-50:500])"""))
        assertEquals(1f, eval("""contains(100, [100:500])"""))
        assertEquals(1f, eval("""contains(300, [100:500])"""))
        assertEquals(1f, eval("""contains(500, [100:500])"""))
        assertEquals(1f, eval("""contains(2147483647, [0:2147483647])"""))
        assertEquals(1f, eval("""contains(1, [0:2147483647])"""))
        assertEquals(1f, eval("""contains(9223372036854775807, [0:9223372036854775807])"""))
        assertEquals(1f, eval("""contains(3, [0:9223372036854775807])"""))
        assertEquals(1f, eval("""contains(-9223372036854775807, [-9223372036854775807:5])"""))

        assertFailsWith<IllegalArgumentException> { eval("""contains(7, [10:0])""") }
        assertFailsWith<IllegalArgumentException> { eval("""contains([0:10])""") }
        assertFailsWith<IllegalArgumentException> { eval("""contains(3, [0:10], 2)""") }
    }

    @Test
    fun logic() {
        assertEquals(0f, eval("""not(true)"""))
        assertEquals(1f, eval("""not(false)"""))
        assertEquals(1f, eval("""and(true, true)"""))
        assertEquals(0f, eval("""and(true, true, true, true, false)"""))
        assertEquals(0f, eval("""or(false, false)"""))
        assertEquals(1f, eval("""or(false, false, false, false, true)"""))
        assertEquals(0.6f, eval("""if(true, 0.6, 0.4)"""))
        assertEquals(0.4f, eval("""if(false, 0.6, 0.4)"""))

        assertFailsWith<IllegalArgumentException> { eval("""not(true, false)""") }
        assertFailsWith<IllegalArgumentException> { eval("""if(true, false)""") }
        assertFailsWith<IllegalArgumentException> { eval("""not("true")""") }
        assertFailsWith<IllegalArgumentException> { eval("""and(1, 2)""") }
        assertFailsWith<IllegalArgumentException> { eval("""or([1], [2])""") }
        assertFailsWith<IllegalArgumentException> { eval("""if(1, true, false)""") }
    }

    @Test
    fun arithmetic() {
        assertEquals(3f, eval("""plus(1, 2)"""))
        assertEquals(3f, eval("""plus(1.0, 2.0)"""))
        assertEquals(-1f, eval("""minus(3, 4)"""))
        assertEquals(-1f, eval("""minus(3.0, 4.0)"""))
        assertEquals(30f, eval("""times(5, 6)"""))
        assertEquals(30f, eval("""times(5.0, 6.0)"""))
        assertEquals(0.875f, eval("""div(7, 8)"""))
        assertEquals(0.875f, eval("""div(7.0, 8.0)"""))
        assertEquals(1f, eval("""div(8, 8)"""))
        assertEquals(7f, eval("""rem(7, 8)"""))
        assertEquals(7f, eval("""rem(7.0, 8.0)"""))

        assertFailsWith<IllegalArgumentException> { eval("""plus(true, false)""") }
        assertFailsWith<IllegalArgumentException> { eval("""plus([1], [2])""") }
        assertFailsWith<IllegalArgumentException> { eval("""plus([1], 2)""") }
    }

    @Test
    fun math() {
        assertEquals(0.3010299f, eval("""log(2)"""), 0.00001f)
        assertEquals(0.63092977f, eval("""log(2, 3)"""), 0.00001f)
        assertEquals(0.6931472f, eval("""ln(2)"""), 0.00001f)
        assertEquals(8f, eval("""pow(2, 3)"""), 0.00001f)
        assertEquals(7.389056f, eval("""exp(2)"""), 0.00001f)
        assertEquals(3.5f, eval("""map(0.75, 0, 1, 2, 4)"""), 0.00001f)

        assertFailsWith<IllegalArgumentException> { eval("""log(1, 2, 3)""") }
        assertFailsWith<IllegalArgumentException> { eval("""pow(2)""") }
        assertFailsWith<IllegalArgumentException> { eval("""log("2")""") }
        assertFailsWith<IllegalArgumentException> { eval("""ln(true)""") }
        assertFailsWith<IllegalArgumentException> { eval("""pow([1], 3)""") }
        assertFailsWith<IllegalArgumentException> { eval("""exp("1")""") }
    }

    @Test
    fun ipv4() {
        assertEquals("3232235776".toBigInteger(), Ip.Numeric("192.168.1.0").value)
        assertEquals("4294967295".toBigInteger(), Ip.Numeric("255.255.255.255").value)
        assertEquals("0".toBigInteger(), Ip.Numeric("0.0.0.0").value)
        assertEquals("16843009".toBigInteger(), Ip.Numeric("1.1.1.1").value)

        assertFailsWith<IllegalArgumentException> { Ip.Numeric("10.0.0").value }
        assertFailsWith<IllegalArgumentException> { Ip.Numeric("10.0.0.0.0").value }
        assertFailsWith<IllegalArgumentException> { Ip.Numeric("10.0.0.-1").value }
        assertFailsWith<IllegalArgumentException> { Ip.Numeric("10.0.0.a").value }
    }

    @Test
    fun ipv4Comparison() {
        val ip1 = Ip.Numeric("192.168.0.200")
        val ip2 = Ip.Numeric("192.168.1.0")
        val ip3 = Ip.Numeric("192.168.1.4")

        assertEquals(false, ip1 > ip3)
        assertEquals(true, ip1 < ip2)
        assertEquals(true, ip2 < ip3)
        assertEquals(true, ip2 == ip2)
    }

    @Test
    fun ipv6() {
        assertEquals(
            "0".toBigInteger(),
            Ip.Numeric("0:0:0:0:0:0:0:0").value
        )
        assertEquals(
            "18446744073709551615".toBigInteger(),
            Ip.Numeric("0:0:0:0:ffff:ffff:ffff:ffff").value
        )
        assertEquals(
            "340282366920938463463374607431768211455".toBigInteger(),
            Ip.Numeric("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff").value
        )
        assertEquals(
            "340282366920938463463374325956791500800".toBigInteger(),
            Ip.Numeric("ffff:ffff:ffff:ffff:ffff::").value
        )
        assertEquals(
            "1095218888755".toBigInteger(),
            Ip.Numeric("::ff:22:33").value
        )
        assertEquals(
            "88269046595092069685019532815630387".toBigInteger(),
            Ip.Numeric("11::ff:22:33").value
        )
    }

    @Test
    fun cidrv4() {
        with(Ip.IpRange("254.200.222.210/23")) {
            assertEquals("4274576896".toBigInteger(), start.value)
            assertEquals("4274577407".toBigInteger(), endInclusive.value)
        }

        with(Ip.IpRange("0.0.0.0/0")) {
            assertEquals("0".toBigInteger(), start.value)
            assertEquals("4294967295".toBigInteger(), endInclusive.value)
        }

        with(Ip.IpRange("254.200.222.210/32")) {
            assertEquals("4274577106".toBigInteger(), start.value)
            assertEquals("4274577106".toBigInteger(), endInclusive.value)
        }
    }

    @Test
    fun cidrv6() {
        with(Ip.IpRange("0000:0000:0000:0000:0000:00ff:0022:0033/122")) {
            assertEquals("1095218888704".toBigInteger(), start.value)
            assertEquals("1095218888767".toBigInteger(), endInclusive.value)
        }

        with(Ip.IpRange("0000:0000:0000:0000:0000:00ff:0022:0033/0")) {
            assertEquals("0".toBigInteger(), start.value)
            assertEquals(
                "340282366920938463463374607431768211455".toBigInteger(),
                endInclusive.value
            )
        }

        with(Ip.IpRange("::00ff:0022:0033/128")) {
            assertEquals("1095218888755".toBigInteger(), start.value)
            assertEquals("1095218888755".toBigInteger(), endInclusive.value)
        }
    }

    @Test
    fun unsupportedFunctions() {
        assertFailsWith<IllegalArgumentException> { eval("""log10(2)""") }
    }

    @Test
    fun composition() {
        assertEquals(1f, eval("""gte(ip("192.168.0.255"), ip("192.168.0.255"))"""))
        assertEquals(1f, eval("""contains(ip("254.200.223.255"), cidr("254.200.222.210/23"))"""))

        assertEquals(0f, eval("""contains(ip("::00ff:0021:FFFF"), cidr("::ff:22:33/122"))"""))
        assertEquals(1f, eval("""contains(ip("::ff:22:3C"), cidr("::ff:22:33/122"))"""))
        assertEquals(1f, eval("""contains(ip("::ff:22:01"), cidr("::ff:22:33/122"))"""))

        assertEquals(0f, eval("""if(gte(datetime("2021-06-01"), datetime("2021-05-31")), 0, 1)"""))
        assertEquals(0f, eval("""log(if(gte(datetime("2021-06-01"), now()), 0, 1))"""))
        assertEquals(
            0f,
            eval(
                """
                |contains(
                |now(),
                |[datetime("2021-06-01"):datetime("2021-06-20")]) 
                |""".trimMargin()
            )
        )
        assertEquals(1f, eval("""if(gt(plus(now(), 1), div(now(), 1)), minus(2, 1), 0)"""))
        assertEquals(
            1f,
            eval(
                """
                |contains(
                |now(),
                |[datetime("2021-06-01"):datetime("2022-06-20")])
                |""".trimMargin()
            )
        )
        assertEquals(
            3 / 7f,
            eval(
                """
                |map(
                |datetime("2021-11-11"),
                |datetime("2021-11-08"), datetime("2021-11-15"),
                |0, 1)
                |""".trimMargin()
            )
        )
    }

    private fun eval(formula: String, env: JsonObject = baseEnv) = doist.ffs.rule.eval(formula, env)

    private fun JsonObjectBuilder.put(key: String, values: List<String>): JsonElement? =
        put(
            key,
            buildJsonArray {
                values.forEach {
                    add(JsonPrimitive(it))
                }
            }
        )
}
