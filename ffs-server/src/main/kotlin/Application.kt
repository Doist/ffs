package doist.ffs

import com.squareup.sqldelight.sqlite.driver.asJdbcDriver
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import doist.ffs.auth.AuthorizationException
import doist.ffs.auth.Permission
import doist.ffs.auth.Session
import doist.ffs.auth.TokenPrincipal
import doist.ffs.auth.UserPrincipal
import doist.ffs.auth.permissions
import doist.ffs.auth.scheme
import doist.ffs.db.fromToken
import doist.ffs.db.members
import doist.ffs.db.tokens
import doist.ffs.endpoints.AuthScheme
import doist.ffs.plugins.Database
import doist.ffs.plugins.database
import doist.ffs.routes.installFlagRoutes
import doist.ffs.routes.installOrganizationRoutes
import doist.ffs.routes.installProjectRoutes
import doist.ffs.routes.installUserRoutes
import doist.ffs.serialization.cbor
import doist.ffs.serialization.json
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.serialization
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.session
import io.ktor.server.cio.EngineMain
import io.ktor.server.plugins.CORS
import io.ktor.server.plugins.CallLogging
import io.ktor.server.plugins.Compression
import io.ktor.server.plugins.ContentNegotiation
import io.ktor.server.plugins.DefaultHeaders
import io.ktor.server.plugins.StatusPages
import io.ktor.server.resources.Resources
import io.ktor.server.response.respond
import io.ktor.server.routing.IgnoreTrailingSlash
import io.ktor.server.sessions.SessionTransportTransformer
import io.ktor.server.sessions.SessionTransportTransformerMessageAuthentication
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.directorySessionStorage
import io.ktor.server.sessions.header
import io.ktor.util.hex
import routes.installTokenRoutes
import java.io.File
import java.sql.SQLException
import java.util.Properties
import kotlin.random.Random

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("Unused")
fun Application.module() {
    installPlugins()
    installDatabase()
    installAuthentication()
    installExceptionHandling()
    installRoutes()
}

fun Application.installPlugins() {
    install(Resources)
    install(IgnoreTrailingSlash)
    install(CallLogging)
    install(CORS) {
        // Cross-origin requests are allowed.
        anyHost()
    }
    install(DefaultHeaders)
    install(ContentNegotiation) {
        serialization(ContentType.Application.Json, json)
        serialization(ContentType.Application.Cbor, cbor)
    }
    install(Compression)
}

fun Application.installDatabase() = install(Database) {
    val hikariOptions = Properties().apply {
        environment.config.keys()
            .filter { key ->
                key.startsWith("hikari.")
            }
            .forEach { key ->
                set(key.removePrefix("hikari."), environment.config.property(key).getString())
            }
    }
    driver = HikariDataSource(HikariConfig(hikariOptions)).asJdbcDriver()
}

fun Application.installAuthentication() {
    install(Sessions) {
        val signKey = environment.config.propertyOrNull("ktor.security.sessions.signKey")?.let {
            hex(it.getString())
        } ?: Random.nextBytes(HMAC_SECRET_KEY_SIZE)

        // Reuse authorization header with a "Session" scheme for session authorization.
        header<Session>(
            HttpHeaders.Authorization,
            directorySessionStorage(File("build/.sessions"))
        ) {
            val base = SessionTransportTransformerMessageAuthentication(signKey)
            transform(object : SessionTransportTransformer {
                val prefix = "${AuthScheme.Session} "

                override fun transformRead(transportValue: String) =
                    if (transportValue.startsWith(prefix)) {
                        base.transformRead(transportValue.substring(prefix.length))
                    } else {
                        null
                    }

                override fun transformWrite(transportValue: String) =
                    "$prefix${base.transformWrite(transportValue)}"
            })
        }
    }

    install(Authentication) {
        session<Session>(name = "session") {
            validate { (id) ->
                val userRoles = database.members.selectOrganizationIdProjectIdByUserId(
                    user_id = id
                ).executeAsList()
                return@validate UserPrincipal(
                    id = id,
                    organizationPermissions = userRoles.associate {
                        it.id to it.role.permissions
                    },
                    projectPermissions = userRoles.filter {
                        it.project_id != null
                    }.associate {
                        it.project_id!! to it.role.permissions
                    }
                )
            }
        }

        scheme(scheme = "Token", name = "token") {
            validate { (_, token) ->
                val id = database.tokens.selectProjectIdByToken(token).executeAsOneOrNull()
                if (id != null) {
                    return@validate TokenPrincipal(
                        projectId = id,
                        permission = Permission.fromToken(token)
                    )
                }
                return@validate null
            }
        }
    }
}

fun Application.installExceptionHandling() {
    install(StatusPages) {
        exception<AuthorizationException> { call, _ ->
            call.respond(HttpStatusCode.Forbidden)
        }
        exception<IllegalArgumentException> { call, e ->
            call.respond(HttpStatusCode.BadRequest, e.message ?: "")
        }
        exception<SQLException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest)
        }
    }
}

fun Application.installRoutes() {
    installUserRoutes()
    installOrganizationRoutes()
    installProjectRoutes()
    installFlagRoutes()
    installTokenRoutes()
}

private const val HMAC_SECRET_KEY_SIZE = 64
