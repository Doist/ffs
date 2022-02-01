package doist.ffs.rule

import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.asJust
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.optional
import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.separatedTerms
import com.github.h0tk3y.betterParse.combinators.skip
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDate
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log
import kotlin.math.pow

fun validateFormula(formula: String) = runCatching { RuleGrammar.parseToEnd(formula) }.isSuccess

/**
 * Evaluates the given [formula] resulting in unit interval [0, 1], the frequency of returning true.
 * 0 is always false, 1 is always true, 0.5 is true ~50% of the time, depending on the environment.
 *
 * Example formulas:
 * - Always true: `1` or `true`
 * - True for half of the contexts: `0.5`
 * - True for an email domain: `matches("*@doist.com", env["user.email"])
 * - True for specific UTC offsets: `contains(["+01:00", "+02:00"], env["user.utc_offset"])`
 * - True after a date/time: `gte(now(), datetime("2038-01-19T04:14:07+01:00")`
 * - True if the user is logged in: `not(isblank(env["user.email"]))`
 * - Gradual rollout: `map(datetime("2021-11-08"), datetime("2021-11-16"), 0, 1, now())`
 *
 * @param formula the formula to parse.
 * @param env the environment map. Accepted values are booleans, numbers, strings, or lists of them.
 *
 * @see RuleGrammar
 * @see RuleExpr
 */
fun eval(formula: String, env: JsonObject): Float {
    try {
        return when (val result = RuleGrammar.parseToEnd(formula).eval(env)) {
            is Boolean -> if (result) 1f else 0f
            is Number -> result.toFloat()
            is String -> result.runCatching { toFloat() }.recoverCatching { 0f }.getOrThrow()
            else -> 0f
        }
    } catch (e: ClassCastException) {
        // ClassCastException is expected on wrongly typed parameters,
        // since nextAs() below does not verify the reified type.
        // We use Throwable because JS throws TypeError.
        throw IllegalArgumentException(e)
    }
}

/**
 * Grammar for rules, resembling spreadsheet formulas, combining values and formulas arbitrarily.
 */
private object RuleGrammar : Grammar<RuleExpr<*>>() {
    //region Tokens
    private val trueLiteral by literalToken("true")
    private val falseLiteral by literalToken("false")

    private val lP by literalToken("(")
    private val rP by literalToken(")")
    private val lB by literalToken("[")
    private val rB by literalToken("]")

    private val comma by literalToken(",")
    private val dot by literalToken(".")
    private val colon by literalToken(":")

    private val minus by literalToken("-")
    private val digits by regexToken("\\d+")

    // Quote + unescaped anything + sequence of escaped and unescaped things + quote.
    private val stringLiteral by regexToken("\"[^\"\\\\]*(\\\\.[^\"\\\\]*)*\"")

    private val envLiteral by literalToken("env")

    private val id by regexToken("[a-zA-Z]\\w*")

    @Suppress("unused")
    private val whitespace by regexToken("\\s+", ignore = true)
    //endregion

    //region Parsers
    private val trueExpr = trueLiteral asJust RuleExpr.BooleanExpr(true)
    private val falseExpr = falseLiteral asJust RuleExpr.BooleanExpr(false)
    private val boolean = trueExpr or falseExpr

    private val double =
        optional(minus) and optional(digits) and skip(dot) and digits map { (minus, int, dec) ->
            var value = int?.text?.toDouble() ?: 0.0
            value += "0.${dec.text}".toDouble()
            minus?.let { value = -value }
            RuleExpr.NumberExpr(value)
        }
    private val long = optional(minus) and digits map { (minus, int) ->
        var value = int.text.toLong()
        minus?.let { value = -value }
        RuleExpr.NumberExpr(value)
    }
    private val number = double or long

    private val string = stringLiteral map {
        RuleExpr.StringExpr(it.text)
    }

    private val envValue =
        skip(envLiteral) and skip(lB) and parser(RuleGrammar::string) and skip(rB) map {
            RuleExpr.EnvExpr(it)
        }

    private val array =
        skip(lB) and separatedTerms(parser(RuleGrammar::rootParser), comma, true) and skip(rB) map {
            RuleExpr.ArrayExpr(it)
        }

    private val range =
        skip(lB) and
            parser(RuleGrammar::rootParser) and skip(colon) and parser(RuleGrammar::rootParser) and
            skip(rB) map { (min, max) ->
            RuleExpr.RangeExpr(min, max)
        }

    private val function =
        id and skip(lP) and separatedTerms(
            parser(RuleGrammar::rootParser),
            comma,
            true
        ) and skip(rP) map { (id, args) ->
            RuleExpr.FunctionExpr(id.text, args)
        }
    //endregion

    override val rootParser: Parser<RuleExpr<*>> =
        boolean or number or string or envValue or array or range or function
}

/**
 * Rule expressions to be evaluated.
 */
private sealed class RuleExpr<out T> {
    abstract fun eval(env: JsonObject): T

    // Casting is necessary because JS is lenient and type parameters are only assessed at runtime.
    protected inline fun <reified T> castEval(env: JsonObject): T = eval(env) as T

    data class BooleanExpr(val value: Boolean) : RuleExpr<Boolean>() {
        override fun eval(env: JsonObject) = value
    }

    data class NumberExpr(val value: Number) : RuleExpr<Number>() {
        override fun eval(env: JsonObject) = value
    }

    data class StringExpr(val value: String) : RuleExpr<String>() {
        override fun eval(env: JsonObject) = value.substring(1, value.lastIndex)
    }

    data class EnvExpr(val nameVal: RuleExpr<String>) : RuleExpr<Any?>() {
        override fun eval(env: JsonObject) = coerceType(env[nameVal.castEval(env)])

        /*
         * Coerce types recursively to String, Boolean, Long, Double, or List.
         */
        fun coerceType(value: JsonElement?, canNest: Boolean = true): Any? = when {
            value is JsonPrimitive -> when {
                value is JsonNull -> null
                value.isString -> value.content
                else -> value.booleanOrNull ?: value.longOrNull ?: value.doubleOrNull
            }
            canNest && value is JsonArray -> value.mapNotNull { coerceType(it, false) }
            else -> null
        }
    }

    data class ArrayExpr<out T>(val list: List<RuleExpr<T>>) : RuleExpr<Collection<T>>() {
        override fun eval(env: JsonObject): Collection<T> = list.map { it.eval(env) }
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    data class RangeExpr<out T>(
        val from: RuleExpr<T>,
        val to: RuleExpr<T>
    ) : RuleExpr<Collection<Long>>() {
        override fun eval(env: JsonObject) =
            wrapRangeInCollection(from.castEval<Long>(env)..to.castEval<Long>(env))
    }

    /**
     * Supported functions.
     */
    sealed class FunctionExpr<T> : RuleExpr<T>() {
        //region Info.
        data class IsBlank(val value: RuleExpr<Any?>) : FunctionExpr<Boolean>() {
            override fun eval(env: JsonObject) = when (val result = value.eval(env)) {
                null -> true
                is String -> result.isBlank()
                is Collection<*> -> result.isEmpty()
                else -> false
            }
        }
        //endregion

        //region Operators.
        data class Eq<T>(
            val value1: RuleExpr<T?>,
            val value2: RuleExpr<T?>
        ) : FunctionExpr<Boolean>() {
            override fun eval(env: JsonObject) = value1.eval(env) == value2.eval(env)
        }

        data class Gt<T>(
            val value1: RuleExpr<Comparable<T>>,
            val value2: RuleExpr<T>
        ) : FunctionExpr<Boolean>() {
            override fun eval(env: JsonObject) =
                value1.castEval<Comparable<Any>>(env) > value2.castEval(env)
        }

        data class Gte<T>(
            val value1: RuleExpr<Comparable<T>>,
            val value2: RuleExpr<T>
        ) : FunctionExpr<Boolean>() {
            override fun eval(env: JsonObject) =
                value1.castEval<Comparable<Any>>(env) >= value2.castEval(env)
        }

        data class Lt<T>(
            val value1: RuleExpr<Comparable<T>>,
            val value2: RuleExpr<T>
        ) : FunctionExpr<Boolean>() {
            override fun eval(env: JsonObject) =
                value1.castEval<Comparable<Any>>(env) < value2.castEval(env)
        }

        data class Lte<T>(
            val value1: RuleExpr<Comparable<T>>,
            val value2: RuleExpr<T>
        ) : FunctionExpr<Boolean>() {
            override fun eval(env: JsonObject) =
                value1.castEval<Comparable<Any>>(env) <= value2.castEval(env)
        }
        //endregion

        //region Date.
        object Now : FunctionExpr<Long>() {
            override fun eval(env: JsonObject) = Clock.System.now().epochSeconds
        }

        data class Datetime(val value: RuleExpr<String>) : FunctionExpr<Long>() {
            override fun eval(env: JsonObject): Long {
                val value = value.castEval<String>(env)
                val instant = runCatching {
                    value.toInstant()
                }.recoverCatching {
                    value.toLocalDateTime().toInstant(TimeZone.UTC)
                }.recoverCatching {
                    value.toLocalDate().atStartOfDayIn(TimeZone.UTC)
                }.getOrThrow()
                return instant.epochSeconds
            }
        }
        //endregion

        //region IP Address.
        sealed class IpExpr<T> : FunctionExpr<T>() {
            data class IpFunc(val value: RuleExpr<String>) : FunctionExpr<Ip.Numeric>() {
                override fun eval(env: JsonObject) = Ip.Numeric(value.castEval(env))
            }

            data class CidrFunc(
                val value: RuleExpr<String>
            ) : FunctionExpr<Collection<Ip.Numeric>>() {
                override fun eval(env: JsonObject): Collection<Ip.Numeric> {
                    val ranges = Ip.IpRange(value.castEval(env))

                    return wrapRangeInCollection(ranges)
                }
            }
        }
        //endregion

        //region Text.
        data class Matches(
            val value: RuleExpr<String>,
            val regex: RuleExpr<String>
        ) : FunctionExpr<Boolean>() {
            override fun eval(env: JsonObject) =
                regex.castEval<String>(env).toRegex().matches(value.castEval(env))
        }
        //endregion

        //region Arrays.
        data class Contains<T>(
            val value: RuleExpr<T>,
            val list: RuleExpr<Collection<T>>,
        ) : FunctionExpr<Boolean>() {
            override fun eval(env: JsonObject) =
                list.castEval<Collection<Any>>(env).contains(value.castEval(env))
        }
        //endregion

        //region Logic.
        data class Not(
            val value: RuleExpr<Boolean>
        ) : FunctionExpr<Boolean>() {
            override fun eval(env: JsonObject) = !value.castEval<Boolean>(env)
        }

        data class And(
            val values: List<RuleExpr<Boolean>>
        ) : FunctionExpr<Boolean>() {
            override fun eval(env: JsonObject) = values.all { it.castEval(env) }
        }

        data class Or(
            val values: List<RuleExpr<Boolean>>
        ) : FunctionExpr<Boolean>() {
            override fun eval(env: JsonObject) = values.any { it.castEval(env) }
        }

        data class If<T>(
            val condition: RuleExpr<Boolean>,
            val valueIfTrue: RuleExpr<T>,
            val valueIfFalse: RuleExpr<T>
        ) : FunctionExpr<T>() {
            override fun eval(env: JsonObject) = if (condition.castEval(env)) {
                valueIfTrue.eval(env)
            } else {
                valueIfFalse.eval(env)
            }
        }
        //endregion.

        //region Arithmetic.
        private interface ArithmeticExpr {
            val left: RuleExpr<Number>
            val right: RuleExpr<Number>
            val doubleOp: (Double, Double) -> Number
            val longOp: (Long, Long) -> Number

            fun opEval(env: JsonObject): Number {
                val leftResult = left.castEval<Number>(env)
                val rightResult = right.castEval<Number>(env)
                return if (leftResult is Double || rightResult is Double) {
                    doubleOp(leftResult.toDouble(), rightResult.toDouble())
                } else {
                    longOp(leftResult.toLong(), rightResult.toLong())
                }
            }
        }

        data class Plus(
            override val left: RuleExpr<Number>,
            override val right: RuleExpr<Number>
        ) : FunctionExpr<Number>(), ArithmeticExpr {
            override val doubleOp: (Double, Double) -> Number = Double::plus
            override val longOp: (Long, Long) -> Number = Long::plus

            override fun eval(env: JsonObject) = super.opEval(env)
        }

        data class Minus(
            override val left: RuleExpr<Number>,
            override val right: RuleExpr<Number>
        ) : FunctionExpr<Number>(), ArithmeticExpr {
            override val doubleOp: (Double, Double) -> Number = Double::minus
            override val longOp: (Long, Long) -> Number = Long::minus

            override fun eval(env: JsonObject) = super.opEval(env)
        }

        data class Times(
            override val left: RuleExpr<Number>,
            override val right: RuleExpr<Number>
        ) : FunctionExpr<Number>(), ArithmeticExpr {
            override val doubleOp: (Double, Double) -> Number = Double::times
            override val longOp: (Long, Long) -> Number = Long::times

            override fun eval(env: JsonObject) = super.opEval(env)
        }

        data class Div(
            override val left: RuleExpr<Number>,
            override val right: RuleExpr<Number>
        ) : FunctionExpr<Number>(), ArithmeticExpr {
            override val doubleOp: (Double, Double) -> Number = Double::div
            override val longOp: (Long, Long) -> Number = { l: Long, r: Long ->
                if (l % r == 0L) l / r else l.toDouble() / r
            }

            override fun eval(env: JsonObject) = super.opEval(env)
        }

        data class Rem(
            override val left: RuleExpr<Number>,
            override val right: RuleExpr<Number>
        ) : FunctionExpr<Number>(), ArithmeticExpr {
            override val doubleOp: (Double, Double) -> Number = Double::rem
            override val longOp: (Long, Long) -> Number = Long::rem

            override fun eval(env: JsonObject) = super.opEval(env)
        }
        //endregion

        //region Math.
        data class Log(
            val value: RuleExpr<Number>,
            val base: RuleExpr<Number>
        ) : FunctionExpr<Double>() {
            constructor(value: RuleExpr<Number>) : this(value, NumberExpr(DEFAULT_BASE))

            override fun eval(env: JsonObject) =
                log(value.castEval<Number>(env).toDouble(), base.castEval<Number>(env).toDouble())

            companion object {
                const val DEFAULT_BASE = 10
            }
        }

        data class Ln(
            val value: RuleExpr<Number>,
        ) : FunctionExpr<Double>() {
            override fun eval(env: JsonObject) = ln(value.castEval<Number>(env).toDouble())
        }

        data class Pow(
            val value: RuleExpr<Number>,
            val exponent: RuleExpr<Number>
        ) : FunctionExpr<Double>() {
            override fun eval(env: JsonObject) =
                value.castEval<Number>(env).toDouble()
                    .pow(exponent.castEval<Number>(env).toDouble())
        }

        data class Exp(
            val value: RuleExpr<Number>,
        ) : FunctionExpr<Double>() {
            override fun eval(env: JsonObject) = exp(value.castEval<Number>(env).toDouble())
        }

        data class Map(
            val value: RuleExpr<Number>,
            val inputStart: RuleExpr<Number>,
            val inputEnd: RuleExpr<Number>,
            val outputStart: RuleExpr<Number>,
            val outputEnd: RuleExpr<Number>
        ) : FunctionExpr<Double>() {
            override fun eval(env: JsonObject): Double {
                val inputStartResult = inputStart.castEval<Number>(env).toDouble()
                val outputStartResult = outputStart.castEval<Number>(env).toDouble()
                return (value.castEval<Number>(env).toDouble() - inputStartResult) /
                    (inputEnd.castEval<Number>(env).toDouble() - inputStartResult) *
                    (outputEnd.castEval<Number>(env).toDouble() - outputStartResult) +
                    outputStartResult
            }
        }
        //endregion
    }

    companion object {
        @Suppress("ComplexMethod", "FunctionName", "UNCHECKED_CAST")
        fun FunctionExpr(id: String, args: List<RuleExpr<*>>): RuleExpr.FunctionExpr<*> {
            val it = args.iterator()
            val functionExpr = when (id.lowercase()) {
                "isblank" -> FunctionExpr.IsBlank(it.castNext())
                "eq" -> FunctionExpr.Eq<Any>(it.castNext(), it.castNext())
                "gt" -> FunctionExpr.Gt<Any>(it.castNext(), it.castNext())
                "gte" -> FunctionExpr.Gte<Any>(it.castNext(), it.castNext())
                "lt" -> FunctionExpr.Lt<Any>(it.castNext(), it.castNext())
                "lte" -> FunctionExpr.Lte<Any>(it.castNext(), it.castNext())
                "now" -> FunctionExpr.Now
                "datetime" -> FunctionExpr.Datetime(it.castNext())
                "ip" -> FunctionExpr.IpExpr.IpFunc(it.castNext())
                "cidr" -> FunctionExpr.IpExpr.CidrFunc(it.castNext())
                "matches" -> FunctionExpr.Matches(it.castNext(), it.castNext())
                "contains" -> FunctionExpr.Contains(it.castNext<Collection<*>>(), it.castNext())
                "not" -> FunctionExpr.Not(it.castNext())
                "and" -> {
                    it.fastForward() // Skip the iterator since we're passing the whole list.
                    FunctionExpr.And(args as List<RuleExpr<Boolean>>)
                }
                "or" -> {
                    it.fastForward() // Skip the iterator since we're passing the whole list.
                    FunctionExpr.Or(args as List<RuleExpr<Boolean>>)
                }
                "if" -> FunctionExpr.If(it.castNext(), it.castNext(), it.castNext())
                "plus" -> FunctionExpr.Plus(it.castNext(), it.castNext())
                "minus" -> FunctionExpr.Minus(it.castNext(), it.castNext())
                "times" -> FunctionExpr.Times(it.castNext(), it.castNext())
                "div" -> FunctionExpr.Div(it.castNext(), it.castNext())
                "rem" -> FunctionExpr.Rem(it.castNext(), it.castNext())
                "log" -> it.castNext<Number>().let { arg0 ->
                    if (it.hasNext()) {
                        FunctionExpr.Log(arg0, it.castNext())
                    } else {
                        FunctionExpr.Log(arg0)
                    }
                }
                "ln" -> FunctionExpr.Ln(it.castNext())
                "pow" -> FunctionExpr.Pow(it.castNext(), it.castNext())
                "exp" -> FunctionExpr.Exp(it.castNext())
                "map" -> FunctionExpr.Map(
                    it.castNext(), it.castNext(), it.castNext(), it.castNext(), it.castNext()
                )
                else -> throw IllegalArgumentException("Unknown function: $id")
            }
            if (it.hasNext()) {
                throw IllegalArgumentException(
                    "Too many arguments for function: $id (${args.size})"
                )
            }
            return functionExpr
        }

        @Suppress("UNCHECKED_CAST")
        private inline fun <reified T : Any> Iterator<RuleExpr<*>>.castNext(): RuleExpr<T> {
            if (hasNext()) {
                return next() as RuleExpr<T>
            } else {
                throw IllegalArgumentException(NoSuchElementException())
            }
        }

        private fun Iterator<*>.fastForward() {
            while (hasNext()) next()
        }

        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        private fun <T : Comparable<T>> wrapRangeInCollection(
            range: ClosedRange<T>
        ): Collection<T> {
            if (range.start > range.endInclusive) {
                throw IllegalArgumentException("can't use inverted range")
            }

            return object : ClosedRange<T> by range, Collection<T> {
                override fun containsAll(elements: Collection<T>) = elements.all {
                    range.contains(it)
                }
                override fun contains(value: T) = range.contains(value)
                override val size: Int get() {
                    TODO("Doesn't need this function")
                }
                override fun iterator(): Iterator<T> {
                    TODO("Doesn't need this function")
                }
            }
        }
    }
}

internal class Ip {
    internal data class Numeric(val ipString: String) : Comparable<Numeric> {
        val value: BigInteger by lazy {
            val segments = ipString.expandIpv6().toSegments()

            if (segments.size > 4) {
                segments.fold(BigInteger.fromInt(0)) { acc, seg -> (acc shl 16) + seg.toInt() }
            } else {
                segments.fold(BigInteger.fromInt(0)) { acc, octet -> (acc shl 8) + octet.toShort() }
            }
        }

        override fun compareTo(other: Numeric) = value.compareTo(other.value)
    }

    internal data class IpRange(val cidr: String) : ClosedRange<Numeric> {
        private lateinit var min: Numeric
        private lateinit var max: Numeric

        init {
            val ipWidth = cidr.split('/')
            val segments = ipWidth[0].expandIpv6().toSegments()

            if (segments.size > 4) {
                val width = ipWidth.getOrNull(1)?.toInt() ?: 128
                calculateIpv6Range(segments, width)
            } else {
                val width = ipWidth.getOrNull(1)?.toByte() ?: 32
                calculateIpv4Range(segments, width)
            }
        }

        private fun calculateIpv6Range(segments: List<UShort>, width: Int) {
            val mask = "1".repeat(width) + "0".repeat(128 - width)
            val subnet = mask.chunked(16).map { it.toUShort(2) }

            val min = arrayListOf<UShort>()
            val max = arrayListOf<UShort>()
            segments.forEachIndexed { i, value ->
                min.add(value and subnet[i])
                max.add(value or subnet[i].inv())
            }

            this.min = Numeric(min.joinToString(":") { it.toString(16) })
            this.max = Numeric(max.joinToString(":") { it.toString(16) })
        }

        private fun calculateIpv4Range(octets: List<UShort>, width: Byte) {
            val mask = 0xFFFFFFFF shl (32 - width)
            val subnet =
                listOf(mask shr 24, mask shr 16, mask shr 8, mask).map { it.toUShort() }

            val min = mutableListOf<UByte>(0u, 0u, 0u, 0u)
            val max = mutableListOf<UByte>(0u, 0u, 0u, 0u)
            octets.forEachIndexed { i, value ->
                min[i] = (value and subnet[i]).toUByte()
                max[i] = (value or subnet[i].inv()).toUByte()
            }

            this.min = Numeric(min.joinToString("."))
            this.max = Numeric(max.joinToString("."))
        }

        override val endInclusive: Numeric
            get() = max
        override val start: Numeric
            get() = min
    }

    companion object {
        fun String.toSegments(): List<UShort> {
            lateinit var segments: List<UShort>

            if (contains('.')) {
                segments = split('.').map { it.toUShort() }
                if (segments.size != 4) {
                    throw IllegalArgumentException("invalid IPv4 format")
                }
            } else {
                segments = split(':').map { it.toUShort(16) }
                if (segments.size != 8) {
                    throw IllegalArgumentException("invalid IPv6 format")
                }
            }

            return segments
        }

        fun String.expandIpv6(): String {
            val compactColonIndex = indexOf("::")
            if (compactColonIndex == -1) {
                return this
            }

            val replacementString = if (compactColonIndex == 0 || compactColonIndex == length - 2) {
                val replacementChunk = if (compactColonIndex == 0) "0:" else ":0"

                replacementChunk.repeat(8 - (filter { it == ':' }.count() - 1))
            } else {
                val extendedSegments = filter { it == ':' }.count()

                ":" + "0:".repeat(8 - extendedSegments)
            }

            return replace("::", replacementString)
        }
    }
}
