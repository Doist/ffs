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

private const val KEY_BEARER_AUTH = "BearerAuth"

data class BearerCredential(val token: String) : Credential

/**
 * Represents a Bearer authentication provider.
 * Based on [io.ktor.server.auth.BasicAuthenticationProvider].
 *
 * @property name is the name of the provider, or `null` for a default provider.
 */
class BearerAuthenticationProvider internal constructor(
    configuration: Configuration
) : AuthenticationProvider(configuration) {
    internal val authenticationFunction = configuration.authenticationFunction

    /**
     * Bearer auth configuration.
     */
    class Configuration internal constructor(
        name: String?
    ) : AuthenticationProvider.Configuration(name) {
        internal var authenticationFunction: AuthenticationFunction<BearerCredential> = {
            throw NotImplementedError(
                "Bearer auth validate function missing. Use `bearer { validate { ... } }` to fix."
            )
        }

        /**
         * Sets a validation function that will check given [BearerCredential] instance and return
         * [Principal], or null if credential does not correspond to an authenticated principal.
         */
        fun validate(body: suspend ApplicationCall.(BearerCredential) -> Principal?) {
            authenticationFunction = body
        }
    }
}

/**
 * Installs Bearer Authentication mechanism.
 */
fun Authentication.Configuration.bearer(
    name: String? = null,
    configure: BearerAuthenticationProvider.Configuration.() -> Unit
) {
    val provider = BearerAuthenticationProvider(
        BearerAuthenticationProvider.Configuration(name).apply(configure)
    )
    val authenticate = provider.authenticationFunction

    provider.pipeline.intercept(AuthenticationPipeline.RequestAuthentication) { context ->
        val credentials = call.request.bearerAuthenticationCredentials()
        val principal = credentials?.let { authenticate(call, it) }

        val cause = when {
            credentials == null -> AuthenticationFailedCause.NoCredentials
            principal == null -> AuthenticationFailedCause.InvalidCredentials
            else -> null
        }

        if (cause != null) {
            context.challenge(KEY_BEARER_AUTH, cause) {
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
 * Retrieves Bearer authentication credentials for this [ApplicationRequest].
 */
fun ApplicationRequest.bearerAuthenticationCredentials(): BearerCredential? {
    val authHeader = parseAuthorizationHeader()
    if (
        authHeader is HttpAuthHeader.Single &&
        authHeader.authScheme.equals(AuthScheme.Bearer, ignoreCase = true)
    ) {
        return BearerCredential(authHeader.blob)
    }
    return null
}
