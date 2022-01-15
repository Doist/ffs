package doist.ffs.ext

import io.ktor.server.routing.Route
import io.ktor.server.routing.RouteSelector
import io.ktor.server.routing.RouteSelectorEvaluation
import io.ktor.server.routing.RoutingResolveContext

/**
 * Builds a route to optionally match specified [path], if it exists.
 */
fun Route.optionalRoute(path: String, build: Route.() -> Unit) = createChild(
    OptionalPathSegmentConstantRouteSelector(path.replaceFirstChar { if (it == '/') "" else "$it" })
).apply(build)

/**
 * Evaluates a route against an optional constant path segment.
 * @param value is a value of the path segment
 */
data class OptionalPathSegmentConstantRouteSelector(val value: String) : RouteSelector() {
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int) = when {
        segmentIndex < context.segments.size && context.segments[segmentIndex] == value -> {
            RouteSelectorEvaluation.ConstantPath
        }
        else -> {
            RouteSelectorEvaluation.Missing
        }
    }

    override fun toString() = "$value?"
}
