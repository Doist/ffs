package doist.ffs.rule

import kotlinx.datetime.Clock
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class RuleEvalTest {
    @Test
    fun testBooleans() {
        assert(eval("true") == 1f)
        assert(eval("false") == 0f)
    }

    @Test
    fun testNumbers() {
        assert(eval("0") == 0f)
        assert(eval("0.14159265") == 0.14159265f)
        assert(eval("0.5") == 0.5f)
        assert(eval("1") == 1f)
    }

    @Test
    fun testStrings() {
        assert(eval("\"abc\"") == 0f)
        assert(eval("\"0.5\"") == 0.5f)
        assert(eval("\"1\"") == 1f)
    }

    @Test
    fun testInfo() {
        assert(eval("""isblank("")""") == 1f)
        assert(eval("""isblank("notblank")""") == 0f)
        assert(eval("""isblank(env["user.email"])""") == 1f)
        assert(
            eval(
                """isblank(env["user.email"])""",
                mapOf("user.email" to "goncalo@doist.com")
            ) == 0f
        )
    }

    @Test
    fun testOperators() {
        assert(eval("""eq(1, 0)""") == 0f)
        assert(eval("""eq(0, 0)""") == 1f)
        assert(eval("""eq(1, 0)""") == 0f)
        assert(eval("""eq("1", "0")""") == 0f)
        assert(eval("""eq("0", "0")""") == 1f)
        assert(eval("""eq("0", "1")""") == 0f)

        assert(eval("""gt(1, 0)""") == 1f)
        assert(eval("""gt(env["number"], 0)""", mapOf("number" to 1)) == 1f)
        assert(eval("""gt(0, 0)""") == 0f)
        assert(eval("""gt(0, 1)""") == 0f)
        assert(eval("""gt("1", "0")""") == 1f)
        assert(eval("""gt("0", "0")""") == 0f)
        assert(eval("""gt("0", "1")""") == 0f)

        assert(eval("""gte(1, 0)""") == 1f)
        assert(eval("""gte(0, 0)""") == 1f)
        assert(eval("""gte(0, 1)""") == 0f)
        assert(eval("""gte("1", "0")""") == 1f)
        assert(eval("""gte("0", "0")""") == 1f)
        assert(eval("""gte("0", "1")""") == 0f)

        assert(eval("""lt(1, 0)""") == 0f)
        assert(eval("""lt(0, 0)""") == 0f)
        assert(eval("""lt(0, 1)""") == 1f)
        assert(eval("""lt("1", "0")""") == 0f)
        assert(eval("""lt("0", "0")""") == 0f)
        assert(eval("""lt("0", "1")""") == 1f)

        assert(eval("""lte(1, 0)""") == 0f)
        assert(eval("""lte(0, 0)""") == 1f)
        assert(eval("""lte(0, 1)""") == 1f)
        assert(eval("""lte("1", "0")""") == 0f)
        assert(eval("""lte("0", "0")""") == 1f)
        assert(eval("""lte("0", "1")""") == 1f)

        assertThrows<IllegalArgumentException> { eval("""gt(1)""") }
        assertThrows<IllegalArgumentException> { eval("""gte(1,2,3)""") }
        assertThrows<ClassCastException> { eval("""lt([1,2], [3,4])""") }
        assertThrows<ClassCastException> { eval("""lte(["1", "2"], ["3", "4"])""") }
    }

    @Test
    fun testDates() {
        val now = Clock.System.now().epochSeconds.toFloat()
        assert(eval("""now()""") in now..now + 1)

        assert(eval("""datetime("2010-06-01T22:19:44Z")""") == 1275430780f)
        assert(eval("""datetime("2010-06-01T22:19:44+10:00")""") == 1275394820f)
        assert(eval("""datetime("2010-06-01T22:19:44")""") == 1275430780f)
        assert(eval("""datetime("2010-06-01")""") == 1275350400f)

        assertThrows<IllegalArgumentException> { eval("""datetime("2010-06-01", "2010-06-01")""") }
        assertThrows<IllegalArgumentException> { eval("""datetime("20211022T154439Z")""") }
        assertThrows<IllegalArgumentException> { eval("""datetime("2021-W42")""") }
        assertThrows<IllegalArgumentException> { eval("""datetime("2021")""") }
        assertThrows<IllegalArgumentException> { eval("""datetime("22:19:44")""") }
    }

    @Test
    fun testText() {
        assert(eval("""matches(".+@doist.com", env["user.email"])""") == 0f)
        assert(
            eval(
                """matches(".+@doist.com", env["user.email"])""",
                mapOf("user.email" to "goncalo@doist.com")
            ) == 1f
        )
        assert(
            eval(
                """matches(".+@doist.io", env["user.email"])""",
                mapOf("user.email" to "goncalo@doist.com")
            ) == 0f
        )

        assertThrows<IllegalArgumentException> { eval("""matches("1", "1", "1")""") }
        assertThrows<ClassCastException> { eval("""matches(1, 2)""") }
        assertThrows<ClassCastException> { eval("""matches("1", 2)""") }
        assertThrows<ClassCastException> { eval("""matches(1, "2")""") }
        assertThrows<ClassCastException> { eval("""matches([1, 2], 1)""") }
    }

    @Test
    fun testArrays() {
        assert(eval("""contains(["+01:00", "+02:00"], "+01:00")""") == 1f)
        assert(eval("""contains(["+01:00", "+02:00"], env["user.utc_offset"])""") == 0f)
        assert(
            eval(
                """contains(["+01:00", "+02:00"], env["user.utc_offset"])""",
                mapOf("user.utc_offset" to "+02:00")
            ) == 1f
        )

        assert(eval("""contains([1, 2], 1)""") == 1f)
        assert(eval("""contains([1, 2], 3)""") == 0f)

        assertThrows<IllegalArgumentException> { eval("""contains("+01:00")""") }
        assertThrows<ClassCastException> { eval("""contains("+01:00", "+01:00")""") }
        assertThrows<ClassCastException> { eval("""contains("+01:00", "+01:00")""") }
    }

    @Test
    fun testLogic() {
        assert(eval("""not(true)""") == 0f)
        assert(eval("""not(false)""") == 1f)
        assert(eval("""and(true, true)""") == 1f)
        assert(eval("""and(true, true, true, true, false)""") == 0f)
        assert(eval("""or(false, false)""") == 0f)
        assert(eval("""or(false, false, false, false, true)""") == 1f)
        assert(eval("""if(true, 0.6, 0.4)""") == 0.6f)
        assert(eval("""if(false, 0.6, 0.4)""") == 0.4f)

        assertThrows<IllegalArgumentException> { eval("""not(true, false)""") }
        assertThrows<IllegalArgumentException> { eval("""if(true, false)""") }
        assertThrows<ClassCastException> { eval("""not("true")""") }
        assertThrows<ClassCastException> { eval("""and(1, 2)""") }
        assertThrows<ClassCastException> { eval("""or([1], [2])""") }
        assertThrows<ClassCastException> { eval("""if(1, true, false)""") }
    }

    @Test
    fun testArithmetic() {
        assert(eval("""plus(1, 2)""") == 3f)
        assert(eval("""plus(1.0, 2.0)""") == 3f)
        assert(eval("""minus(3, 4)""") == -1f)
        assert(eval("""minus(3.0, 4.0)""") == -1f)
        assert(eval("""times(5, 6)""") == 30f)
        assert(eval("""times(5.0, 6.0)""") == 30f)
        assert(eval("""div(7, 8)""") == 0.875f)
        assert(eval("""div(7.0, 8.0)""") == 0.875f)
        assert(eval("""div(8, 8)""") == 1f)
        assert(eval("""rem(7, 8)""") == 7f)
        assert(eval("""rem(7.0, 8.0)""") == 7f)

        assertThrows<ClassCastException> { eval("""plus(true, false)""") }
        assertThrows<ClassCastException> { eval("""plus([1], [2])""") }
        assertThrows<ClassCastException> { eval("""plus([1], 2)""") }
    }

    @Test
    fun testMath() {
        assert(eval("""log(2)""") == 0.301029996f)
        assert(eval("""log(2, 3)""") == 0.630929754f)
        assert(eval("""ln(2)""") == 0.693147181f)
        assert(eval("""pow(2, 3)""") == 8f)
        assert(eval("""exp(2)""") == 7.389056099f)
        assert(eval("""map(0.75, 0, 1, 2, 4)""") == 3.5f)

        assertThrows<IllegalArgumentException> { eval("""log(1, 2, 3)""") }
        assertThrows<IllegalArgumentException> { eval("""pow(2)""") }
        assertThrows<ClassCastException> { eval("""log("2")""") }
        assertThrows<ClassCastException> { eval("""ln(true)""") }
        assertThrows<ClassCastException> { eval("""pow([1], 3)""") }
        assertThrows<ClassCastException> { eval("""exp("1")""") }
    }

    @Test
    fun testUnsupportedFunctions() {
        assertThrows<IllegalArgumentException> { eval("""log10(2)""") }
    }

    @Test
    fun testComposition() {
        assert(eval("""if(gte(datetime("2021-06-01"), datetime("2021-05-31")), 0, 1)""") == 0f)
        assert(eval("""log(if(gte(datetime("2021-06-01"), now()), 0, 1))""") == 0f)
        assert(eval("""if(gt(plus(now(), 1), div(now(), 1)), minus(2, 1), 0)""") == 1f)
        assert(
            eval(
                """map(
                    |datetime("2021-11-11"),
                    |datetime("2021-11-08"), datetime("2021-11-15"),
                    |0, 1)""".trimMargin()
            ) == 3 / 7f
        )
    }

    private fun eval(formula: String, env: Map<String, Any> = emptyMap()) =
        doist.ffs.rule.eval(formula, env)
}
