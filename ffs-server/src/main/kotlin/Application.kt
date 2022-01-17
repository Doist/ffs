package doist.ffs

import com.squareup.sqldelight.sqlite.driver.asJdbcDriver
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import doist.ffs.auth.AuthorizationException
import doist.ffs.auth.Permission
import doist.ffs.auth.Session
import doist.ffs.auth.TokenPrincipal
import doist.ffs.auth.UserPrincipal
import doist.ffs.auth.bearer
import doist.ffs.auth.permissions
import doist.ffs.db.fromToken
import doist.ffs.db.roles
import doist.ffs.db.tokens
import doist.ffs.endpoints.Users
import doist.ffs.plugins.Database
import doist.ffs.plugins.database
import doist.ffs.routes.installFlagRoutes
import doist.ffs.routes.installOrganizationRoutes
import doist.ffs.routes.installProjectRoutes
import doist.ffs.routes.installUserRoutes
import doist.ffs.serialization.cbor
import doist.ffs.serialization.json
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.serialization
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.session
import io.ktor.server.cio.EngineMain
import io.ktor.server.plugins.CallLogging
import io.ktor.server.plugins.Compression
import io.ktor.server.plugins.ContentNegotiation
import io.ktor.server.plugins.DefaultHeaders
import io.ktor.server.plugins.StatusPages
import io.ktor.server.resources.Resources
import io.ktor.server.resources.href
import io.ktor.server.response.respond
import io.ktor.server.routing.IgnoreTrailingSlash
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import routes.installTokenRoutes
import java.util.Properties

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("Unused")
fun Application.module() {
    installPlugins()
    installDatabase()
    installAuthentication()
    installRoutes()
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

fun Application.installAuthentication() = install(Authentication) {
    session<Session>("session") {
        validate { (id) ->
            val userRoles = database.roles.selectOrganizationIdProjectIdByUser(
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
        challenge(href(Users.Login()))
    }
    bearer("token") {
        validate { credential ->
            val token = credential.token
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

fun Application.installPlugins() {
    install(Resources)
    install(IgnoreTrailingSlash)
    install(CallLogging)
    install(DefaultHeaders)
    install(ContentNegotiation) {
        serialization(ContentType.Application.Json, json)
        serialization(ContentType.Application.Cbor, cbor)
    }
    install(Compression)
    install(Sessions) {
        cookie<Session>("session")
    }
    install(StatusPages) {
        exception<AuthorizationException> { call, _ ->
            call.respond(HttpStatusCode.Forbidden)
        }
        exception<IllegalArgumentException> { call, _ ->
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
