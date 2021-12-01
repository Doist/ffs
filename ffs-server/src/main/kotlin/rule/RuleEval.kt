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
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDate
import kotlinx.datetime.toLocalDateTime
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log
import kotlin.math.pow
import kotlin.reflect.KClassifier
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.typeOf

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
fun eval(formula: String, env: KMap<String, Any>): Float {
    return when (val result = RuleGrammar.parseToEnd(formula).eval(env)) {
        is Boolean -> if (result) 1f else 0f
        is Number -> result.toFloat()
        is String -> result.runCatching { toFloat() }.recoverCatching { 0f }.getOrThrow()
        else -> 0f
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
        minus?.let { value -= value }
        RuleExpr.NumberExpr(value)
    }
    private val number = double or long

    private val string = stringLiteral map {
        RuleExpr.StringExpr(it.text)
    }

    private val envValue = skip(envLiteral) and skip(lB) and parser(::string) and skip(rB) map {
        RuleExpr.EnvExpr(it)
    }

    private val array =
        skip(lB) and separatedTerms(parser(::rootParser), comma, true) and skip(rB) map {
            RuleExpr.ArrayExpr(it)
        }

    private val function =
        id and skip(lP) and separatedTerms(
            parser(::rootParser),
            comma,
            true
        ) and skip(rP) map { (id, args) ->
            RuleExpr.FunctionExpr(id.text, args)
        }
    //endregion

    override val rootParser: Parser<RuleExpr<*>> =
        boolean or number or string or envValue or array or function
}

/**
 * Rule expressions to be evaluated.
 */
private sealed class RuleExpr<T> {
    abstract fun eval(env: KMap<String, Any>): T

    data class BooleanExpr(val value: Boolean) : RuleExpr<Boolean>() {
        override fun eval(env: KMap<String, Any>) = value
    }

    data class NumberExpr(val value: Number) : RuleExpr<Number>() {
        override fun eval(env: KMap<String, Any>) = value
    }

    data class StringExpr(val value: String) : RuleExpr<String>() {
        override fun eval(env: KMap<String, Any>) = value.substring(1, value.lastIndex)
    }

    data class EnvExpr(val nameVal: RuleExpr<String>) : RuleExpr<Any>() {
        override fun eval(env: KMap<String, Any>) = coerceType(env[nameVal.eval(env)]) ?: ""

        fun coerceType(value: Any?, canNest: Boolean = true): Any? = when {
            // Coerce number types to Long and Double.
            value is Byte -> value.toLong()
            value is Short -> value.toLong()
            value is Int -> value.toLong()
            value is Float -> value.toDouble()
            // Accept Boolean, Long, Double, and String.
            value is Boolean || value is Long || value is Double || value is String -> value
            // Coerce Array into List.
            canNest && value is Array<*> -> value.mapNotNull { coerceType(it, false) }
            // Accept List.
            canNest && value is List<*> -> value.mapNotNull { coerceType(it, false) }
            // Drop everything else.
            else -> null
        }
    }

    data class ArrayExpr(
        val list: List<RuleExpr<*>>
    ) : RuleExpr<List<*>>() {
        override fun eval(env: KMap<String, Any>): List<*> = list.map { it.eval(env) }
    }

    /**
     * Supported functions.
     */
    @Suppress("unused")
    sealed class FunctionExpr<T> : RuleExpr<T>() {
        //region Info.
        data class IsBlank(val value: RuleExpr<Any?>) : FunctionExpr<Boolean>() {
            override fun eval(env: KMap<String, Any>) = when (val result = value.eval(env)) {
                is String -> result.isBlank()
                is List<*> -> result.isEmpty()
                else -> result == null
            }
        }
        //endregion

        //region Operators.
        data class Eq(
            val value1: Any,
            val value2: Any
        ) : FunctionExpr<Boolean>() {
            override fun eval(env: KMap<String, Any>) = value1 == value2
        }

        data class Gt(
            val value1: RuleExpr<Comparable<Any>>,
            val value2: RuleExpr<Comparable<Any>>
        ) : FunctionExpr<Boolean>() {
            override fun eval(env: KMap<String, Any>) = value1.eval(env) > value2.eval(env)
        }

        data class Gte(
            val value1: RuleExpr<Comparable<Any>>,
            val value2: RuleExpr<Comparable<Any>>
        ) : FunctionExpr<Boolean>() {
            override fun eval(env: KMap<String, Any>) = value1.eval(env) >= value2.eval(env)
        }

        data class Lt(
            val value1: RuleExpr<Comparable<Any>>,
            val value2: RuleExpr<Comparable<Any>>
        ) : FunctionExpr<Boolean>() {
            override fun eval(env: KMap<String, Any>) = value1.eval(env) < value2.eval(env)
        }

        data class Lte(
            val value1: RuleExpr<Comparable<Any>>,
            val value2: RuleExpr<Comparable<Any>>
        ) : FunctionExpr<Boolean>() {
            override fun eval(env: KMap<String, Any>) = value1.eval(env) <= value2.eval(env)
        }
        //endregion

        //region Dates.
        object Now : FunctionExpr<Long>() {
            override fun eval(env: KMap<String, Any>) = Clock.System.now().epochSeconds
        }

        data class Datetime(val value: RuleExpr<String>) : FunctionExpr<Long>() {
            override fun eval(env: KMap<String, Any>): Long {
                val value = value.eval(env)
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

        //region Text.
        data class Matches(
            val regex: RuleExpr<String>,
            val value: RuleExpr<String>
        ) : FunctionExpr<Boolean>() {
            override fun eval(env: KMap<String, Any>) =
                regex.eval(env).toRegex().matches(value.eval(env))
        }
        //endregion

        //region Arrays.
        data class Contains<T>(
            val list: RuleExpr<List<T>>,
            val value: RuleExpr<T>
        ) : FunctionExpr<Boolean>() {
            override fun eval(env: KMap<String, Any>) = list.eval(env).contains(value.eval(env))
        }
        //endregion

        //region Logic.
        data class Not(
            val value: RuleExpr<Boolean>
        ) : FunctionExpr<Boolean>() {
            override fun eval(env: KMap<String, Any>) = !value.eval(env)
        }

        data class And(
            val values: List<RuleExpr<Boolean>>
        ) : FunctionExpr<Boolean>() {
            override fun eval(env: KMap<String, Any>) = values.all { it.eval(env) }
        }

        data class Or(
            val values: List<RuleExpr<Boolean>>
        ) : FunctionExpr<Boolean>() {
            override fun eval(env: KMap<String, Any>) = values.any { it.eval(env) }
        }

        data class If<T>(
            val condition: RuleExpr<Boolean>,
            val valueIfTrue: RuleExpr<T>,
            val valueIfFalse: RuleExpr<T>
        ) : FunctionExpr<T>() {
            override fun eval(env: KMap<String, Any>) = if (condition.eval(env)) {
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

            fun eval(env: KMap<String, Any>): Number {
                val leftResult = left.eval(env)
                val rightResult = right.eval(env)
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

            override fun eval(env: KMap<String, Any>) = super.eval(env)
        }

        data class Minus(
            override val left: RuleExpr<Number>,
            override val right: RuleExpr<Number>
        ) : FunctionExpr<Number>(), ArithmeticExpr {
            override val doubleOp: (Double, Double) -> Number = Double::minus
            override val longOp: (Long, Long) -> Number = Long::minus

            override fun eval(env: KMap<String, Any>) = super.eval(env)
        }

        data class Times(
            override val left: RuleExpr<Number>,
            override val right: RuleExpr<Number>
        ) : FunctionExpr<Number>(), ArithmeticExpr {
            override val doubleOp: (Double, Double) -> Number = Double::times
            override val longOp: (Long, Long) -> Number = Long::times

            override fun eval(env: KMap<String, Any>) = super.eval(env)
        }

        data class Div(
            override val left: RuleExpr<Number>,
            override val right: RuleExpr<Number>
        ) : FunctionExpr<Number>(), ArithmeticExpr {
            override val doubleOp: (Double, Double) -> Number = Double::div
            override val longOp: (Long, Long) -> Number = { l: Long, r: Long ->
                if (l % r == 0L) l / r else l.toDouble() / r
            }

            override fun eval(env: KMap<String, Any>) = super.eval(env)
        }

        data class Rem(
            override val left: RuleExpr<Number>,
            override val right: RuleExpr<Number>
        ) : FunctionExpr<Number>(), ArithmeticExpr {
            override val doubleOp: (Double, Double) -> Number = Double::rem
            override val longOp: (Long, Long) -> Number = Long::rem

            override fun eval(env: KMap<String, Any>) = super.eval(env)
        }
        //endregion

        //region Math.
        data class Log(
            val value: RuleExpr<Number>,
            val base: RuleExpr<Number>
        ) : FunctionExpr<Double>() {
            constructor(value: RuleExpr<Number>) : this(value, NumberExpr(DEFAULT_BASE))

            override fun eval(env: KMap<String, Any>) =
                log(value.eval(env).toDouble(), base.eval(env).toDouble())

            companion object {
                const val DEFAULT_BASE = 10
            }
        }

        data class Ln(
            val value: RuleExpr<Number>,
        ) : FunctionExpr<Double>() {
            override fun eval(env: KMap<String, Any>) = ln(value.eval(env).toDouble())
        }

        data class Pow(
            val value: RuleExpr<Number>,
            val exponent: RuleExpr<Number>
        ) : FunctionExpr<Double>() {
            override fun eval(env: KMap<String, Any>) =
                value.eval(env).toDouble().pow(exponent.eval(env).toDouble())
        }

        data class Exp(
            val value: RuleExpr<Number>,
        ) : FunctionExpr<Double>() {
            override fun eval(env: KMap<String, Any>) = exp(value.eval(env).toDouble())
        }

        data class Map(
            val inputStart: RuleExpr<Number>,
            val inputEnd: RuleExpr<Number>,
            val outputStart: RuleExpr<Number>,
            val outputEnd: RuleExpr<Number>,
            val value: RuleExpr<Number>
        ) : FunctionExpr<Double>() {
            override fun eval(env: KMap<String, Any>): Double {
                val inputStartResult = inputStart.eval(env).toDouble()
                val outputStartResult = outputStart.eval(env).toDouble()
                return (value.eval(env).toDouble() - inputStartResult) /
                    (inputEnd.eval(env).toDouble() - inputStartResult) *
                    (outputEnd.eval(env).toDouble() - outputStartResult) +
                    outputStartResult
            }
        }
        //endregion
    }

    @OptIn(ExperimentalStdlibApi::class)
    companion object {
        private val CLASSIFIER_LIST: KClassifier
        private val CLASSIFIER_EXPR: KClassifier

        init {
            val type = typeOf<List<RuleExpr<*>>>()
            CLASSIFIER_LIST = type.classifier!!
            CLASSIFIER_EXPR = type.arguments[0].type!!.classifier!!
        }

        @Suppress("FunctionName", "ReturnCount")
        fun FunctionExpr(id: String, args: List<RuleExpr<*>>): FunctionExpr<*> {
            // Find function expression class for `id`.
            val cls = FunctionExpr::class.sealedSubclasses.find {
                id.equals(it.simpleName, ignoreCase = true)
            } ?: throw IllegalArgumentException("Unsupported function \"$id\"")

            // Use its object instance, if it's an object.
            cls.objectInstance?.let {
                return it
            }

            // Instantiate by spreading `args`, if a constructor matches argument count.
            // Optimize common cases since `toTypedArray` copies, and so does *spreading.
            val argCount = args.size
            cls.constructors.find { it.parameters.size == argCount }?.let {
                @Suppress("SpreadOperator", "MagicNumber")
                return when (argCount) {
                    0 -> it.call()
                    1 -> it.call(args[0])
                    2 -> it.call(args[0], args[1])
                    3 -> it.call(args[0], args[1], args[2])
                    else -> it.call(*args.toTypedArray())
                }
            }

            // Instantiate with `args` directly, if a constructor takes a list of expressions.
            cls.primaryConstructor?.let {
                val params = it.parameters
                if (params.size != 1) return@let
                val type = params[0].type
                if (type.classifier != CLASSIFIER_LIST) return@let
                if (type.arguments.getOrNull(0)?.type?.classifier != CLASSIFIER_EXPR) return@let
                return it.call(args)
            }

            throw IllegalArgumentException("Unsupported arguments for \"$id\"")
        }
    }
}

// Alias Map<K, V> to avoid clashes with map function expression.
typealias KMap<K, V> = Map<K, V>
