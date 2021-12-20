package rule

import doist.ffs.rule.eval
import kotlinx.benchmark.Scope
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.State

@State(Scope.Benchmark)
open class RuleEvalBenchmark {
    @Param(
        """if(gte(now(), datetime("2021-10-22")), 1, 0)""",
        """div(minus(now(), datetime("2021-11-08")), 7)""",
        """or(matches(".+@doist.com", env["user.email"]), eq(true, env["user.beta"]))"""
    )
    lateinit var formula: String

    private val env = mapOf(
        "user.email" to JsonPrimitive("goncalo@doist.com"),
        "user.utc_offset" to JsonPrimitive("+01:00"),
        "user.locale" to JsonPrimitive("pt-PT")
    )

    @Benchmark
    fun eval(): Float {
        return eval(formula, JsonObject(env))
    }
}
