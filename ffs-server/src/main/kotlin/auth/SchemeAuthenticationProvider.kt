package doist.ffs.auth

import io.ktor.http.auth.AuthScheme
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.AuthenticationFailedCause
import io.ktor.server.auth.AuthenticationFunction
import io.ktor.server.auth.AuthenticationPipeline
import io.ktor.server.auth.AuthenticationProvider
import io.ktor.server.auth.Credential
import io.ktor.server.auth.Principal
import io.ktor.server.auth.UnauthorizedResponse
import io.ktor.server.auth.parseAuthorizationHeader
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.response.respond

private const val KEY_SCHEME_AUTH = "SchemeAuth"

data class SchemeCredential(val scheme: String, val token: String) : Credential

/**
 * Represents a scheme authentication provider.
 * Based on [io.ktor.server.auth.BasicAuthenticationProvider].
 *
 * @property scheme the auth scheme, from [AuthScheme] or custom.
 * @property name is the name of the provider, or `null` for a default provider.
 */
class SchemeAuthenticationProvider internal constructor(
    configuration: Configuration
) : AuthenticationProvider(configuration) {
    internal val authenticationFunction = configuration.authenticationFunction

    /**
     * Scheme auth configuration.
     */
    class Configuration internal constructor(
        name: String?
    ) : AuthenticationProvider.Configuration(name) {
        internal var authenticationFunction: AuthenticationFunction<SchemeCredential> = {
            throw NotImplementedError(
                "scheme validate function missing. Use `scheme(...) { validate { ... } }` to fix."
            )
        }

        /**
         * Sets a validation function that will check given [SchemeCredential] instance and return
         * [Principal], or null if credential does not correspond to an authenticated principal.
         */
        fun validate(body: suspend ApplicationCall.(SchemeCredential) -> Principal?) {
            authenticationFunction = body
        }
    }
}

/**
 * Installs Scheme Authentication mechanism.
 */
fun Authentication.Configuration.scheme(
    scheme: String,
    name: String? = null,
    configure: SchemeAuthenticationProvider.Configuration.() -> Unit
) {
    val provider = SchemeAuthenticationProvider(
        SchemeAuthenticationProvider.Configuration(name).apply(configure)
    )
    val authenticate = provider.authenticationFunction

    provider.pipeline.intercept(AuthenticationPipeline.RequestAuthentication) { context ->
        val credentials = call.request.schemeAuthenticationCredentials(scheme)
        val principal = credentials?.let { authenticate(call, it) }

        val cause = when {
            credentials == null -> AuthenticationFailedCause.NoCredentials
            principal == null -> AuthenticationFailedCause.InvalidCredentials
            else -> null
        }

        if (cause != null) {
            context.challenge(KEY_SCHEME_AUTH, cause) {
                call.respond(UnauthorizedResponse())
                it.complete()
            }
        }
        if (principal != null) {
            context.principal(principal)
        }
    }

    register(provider)
}

/**
 * Retrieves scheme's authentication credentials for this [ApplicationRequest].
 */
fun ApplicationRequest.schemeAuthenticationCredentials(scheme: String): SchemeCredential? {
    val authHeader = parseAuthorizationHeader()
    if (
        authHeader is HttpAuthHeader.Single &&
        authHeader.authScheme.equals(scheme, ignoreCase = true)
    ) {
        return SchemeCredential(scheme, authHeader.blob)
    }
    return null
}
