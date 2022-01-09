package doist.ffs

import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import doist.ffs.auth.AuthorizationException
import doist.ffs.auth.Permission
import doist.ffs.auth.TokenPrincipal
import doist.ffs.auth.UserPrincipal
import doist.ffs.auth.bearer
import doist.ffs.auth.permissions
import doist.ffs.db.fromToken
import doist.ffs.db.roles
import doist.ffs.db.tokens
import doist.ffs.plugins.Database
import doist.ffs.plugins.database
import doist.ffs.routes.PATH_LOGIN
import doist.ffs.routes.PATH_USERS
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
import io.ktor.server.response.respond
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    installPlugins()
    installRoutes()
}

fun Application.installPlugins() {
    install(Database) {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    }
    install(CallLogging)
    install(DefaultHeaders)
    install(ContentNegotiation) {
        serialization(ContentType.Application.Json, json)
        serialization(ContentType.Application.Cbor, cbor)
    }
    install(Compression)
    install(Sessions) {
        cookie<Long>("user_session")
    }
    install(Authentication) {
        session<Long>("session") {
            validate { id ->
                val userRoles = database.roles.selectOrganizationIdProjectIdByUser(
                    user_id = id
                ).executeAsList()
                if (userRoles.isNotEmpty()) {
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
                return@validate null
            }
            challenge("$PATH_USERS/$PATH_LOGIN")
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
    install(StatusPages) {
        exception<AuthorizationException> { call, _ ->
            call.respond(HttpStatusCode.Forbidden)
        }
    }
}

fun Application.installRoutes() {
    installOrganizationRoutes()
    installProjectRoutes()
    installFlagRoutes()
    installUserRoutes()
}
