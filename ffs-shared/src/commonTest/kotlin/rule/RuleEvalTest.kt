package doist.ffs.rule

import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RuleEvalTest {
    @Test
    fun testBooleans() {
        assertEquals(1f, eval("true"))
        assertEquals(0f, eval("false"))
    }

    @Test
    fun testNumbers() {
        assertEquals(0f, eval("0"))
        assertEquals(0.14159265f, eval("0.14159265"), 0.00001f)
        assertEquals(0.5f, eval("0.5"))
        assertEquals(1f, eval("1"))
    }

    @Test
    fun testStrings() {
        assertEquals(0f, eval("\"abc\""))
        assertEquals(0.5f, eval("\"0.5\""))
        assertEquals(1f, eval("\"1\""))
    }

    @Test
    fun testEnv() {
        assertEquals(0f, eval("""env["b"]"""))
        assertEquals(0f, eval("""env["b"]""", mapOf("b" to false)))
        assertEquals(1f, eval("""env["b"]""", mapOf("b" to true)))
        assertEquals(0f, eval("""env["n"]""", mapOf("n" to 0f)))
        assertEquals(0.5f, eval("""env["n"]""", mapOf("n" to 0.5f)))
        assertEquals(1f, eval("""env["n"]""", mapOf("n" to 1f)))
        assertEquals(0f, eval("""env["s"]""", mapOf("s" to "0")))
        assertEquals(0.5f, eval("""env["s"]""", mapOf("s" to "0.5")))
        assertEquals(1f, eval("""env["s"]""", mapOf("s" to "1")))
        assertEquals(0f, eval("""contains(env["l"], "d")""", mapOf("l" to listOf("a", "b", "c"))))
        assertEquals(1f, eval("""contains(env["l"], "b")""", mapOf("l" to listOf("a", "b", "c"))))

        assertEquals(1f, eval("""isblank(env["i"])""", mapOf("i" to mapOf("a" to "b"))))
        assertEquals(1f, eval("""isblank(env["i"])""", mapOf("i" to listOf(listOf("a", "b")))))
    }

    @Test
    fun testFail() {
        assertEquals(1f, eval("""isblank(env["i"])""", mapOf("i" to listOf(listOf("a", "b")))))
    }

    @Test
    fun testInfo() {
        assertEquals(1f, eval("""isblank("")"""))
        assertEquals(0f, eval("""isblank("notblank")"""))
    }

    @Test
    fun testOperators() {
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
    fun testDates() {
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
    fun testText() {
        assertEquals(0f, eval("""matches(".+@doist.com", "goncalo@doist.io")"""))
        assertEquals(1f, eval("""matches(".+@doist.com", "goncalo@doist.com")"""))

        assertFailsWith<IllegalArgumentException> { eval("""matches("1", "1", "1")""") }
        assertFailsWith<IllegalArgumentException> { eval("""matches(1, 2)""") }
        assertFailsWith<IllegalArgumentException> { eval("""matches("1", 2)""") }
        assertFailsWith<IllegalArgumentException> { eval("""matches(1, "2")""") }
        assertFailsWith<IllegalArgumentException> { eval("""matches([1, 2], 1)""") }
    }

    @Test
    fun testArrays() {
        assertEquals(0f, eval("""contains(["+01:00", "+02:00"], "+00:00")"""))
        assertEquals(1f, eval("""contains(["+01:00", "+02:00"], "+01:00")"""))

        assertEquals(1f, eval("""contains([1, 2], 1)"""))
        assertEquals(0f, eval("""contains([1, 2], 3)"""))

        assertFailsWith<IllegalArgumentException> { eval("""contains("+01:00")""") }
        assertFailsWith<IllegalArgumentException> { eval("""contains("+01:00", "+01:00")""") }
        assertFailsWith<IllegalArgumentException> { eval("""contains("+01:00", "+01:00")""") }
    }

    @Test
    fun testLogic() {
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
    fun testArithmetic() {
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
    fun testMath() {
        assertEquals(0.3010299f, eval("""log(2)"""), 0.00001f)
        assertEquals(0.63092977f, eval("""log(2, 3)"""), 0.00001f)
        assertEquals(0.6931472f, eval("""ln(2)"""), 0.00001f)
        assertEquals(8f, eval("""pow(2, 3)"""), 0.00001f)
        assertEquals(7.389056f, eval("""exp(2)"""), 0.00001f)
        assertEquals(3.5f, eval("""map(0, 1, 2, 4, 0.75)"""), 0.00001f)

        assertFailsWith<IllegalArgumentException> { eval("""log(1, 2, 3)""") }
        assertFailsWith<IllegalArgumentException> { eval("""pow(2)""") }
        assertFailsWith<IllegalArgumentException> { eval("""log("2")""") }
        assertFailsWith<IllegalArgumentException> { eval("""ln(true)""") }
        assertFailsWith<IllegalArgumentException> { eval("""pow([1], 3)""") }
        assertFailsWith<IllegalArgumentException> { eval("""exp("1")""") }
    }

    @Test
    fun testUnsupportedFunctions() {
        assertFailsWith<IllegalArgumentException> { eval("""log10(2)""") }
    }

    @Test
    fun testComposition() {
        assertEquals(0f, eval("""if(gte(datetime("2021-06-01"), datetime("2021-05-31")), 0, 1)"""))
        assertEquals(0f, eval("""log(if(gte(datetime("2021-06-01"), now()), 0, 1))"""))
        assertEquals(1f, eval("""if(gt(plus(now(), 1), div(now(), 1)), minus(2, 1), 0)"""))
        assertEquals(
            3 / 7f,
            eval("""
                |map(
                |datetime("2021-11-08"), datetime("2021-11-15"),
                |0, 1,
                |datetime("2021-11-11")
                |)
                |""".trimMargin()
            )
        )
    }

    private fun eval(formula: String, env: Map<String, Any> = emptyMap()) =
        doist.ffs.rule.eval(formula, env)
}
