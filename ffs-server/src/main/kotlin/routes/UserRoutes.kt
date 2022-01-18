package doist.ffs.routes

import doist.ffs.auth.Argon2Password
import doist.ffs.auth.Session
import doist.ffs.db.UserQueries
import doist.ffs.db.capturingLastInsertId
import doist.ffs.db.users
import doist.ffs.endpoints.Users
import doist.ffs.ext.authorizeForUser
import doist.ffs.ext.href
import doist.ffs.ext.optionalRoute
import doist.ffs.plugins.database
import doist.ffs.validators.validateEmail
import doist.ffs.validators.validatePassword
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receiveParameters
import io.ktor.server.resources.delete
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.routing
import io.ktor.server.sessions.clear
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import io.ktor.server.util.getOrFail
import kotlinx.coroutines.delay
import routes.PATH_LATEST
import kotlin.random.Random

fun Application.installUserRoutes() = routing {
    optionalRoute(PATH_LATEST) {
        registerUser()
        loginUser()
        logoutUser()

        authenticate("session") {
            updateUser()
            deleteUser()
        }
    }
}

/**
 * Register a user.
 */
private fun Route.registerUser() = post<Users.Register> {
    val params = call.receiveParameters()
    val name = params.getOrFail(Users.NAME)
    val email = params.getOrFail(Users.EMAIL)
    val password = params.getOrFail(Users.PASSWORD)

    if (validateEmail(email) && validatePassword(password)) {
        val id = database.capturingLastInsertId {
            users.insert(name = name, email = email, password = Argon2Password.encode(password))
        }
        call.sessions.set(Session(id))
        call.response.header(HttpHeaders.Location, href(Users.ById(id = id)))
        call.respond(HttpStatusCode.Created, database.users.selectById(id = id).executeAsOne())
    } else {
        call.respond(HttpStatusCode.BadRequest)
    }
}

/**
 * Log in a user.
 */
private fun Route.loginUser() = post<Users.Login> {
    val params = call.receiveParameters()
    val email = params.getOrFail(Users.EMAIL)
    val password = params.getOrFail(Users.PASSWORD)

    val id = database.users.selectIdByEmail(email = email).executeAsOneOrNull()
    if (id != null && database.users.testPassword(id, password)) {
        call.sessions.set(Session(id))
        call.respond(HttpStatusCode.OK, database.users.selectById(id = id).executeAsOne())
    } else {
        call.respond(HttpStatusCode.Unauthorized)
    }
}

/**
 * Log out a user.
 *
 * Always responds with `200 OK`.
 */
private fun Route.logoutUser() = post<Users.Logout> {
    call.sessions.clear<Session>()
    call.respond(HttpStatusCode.OK)
}

/**
 * Update a user.
 */
private fun Route.updateUser() = put<Users.ById> { (_, id) ->
    authorizeForUser(id = id)

    val params = call.receiveParameters()
    val currentPassword = params[Users.CURRENT_PASSWORD]
    val name = params[Users.NAME]
    val email = params[Users.EMAIL]
    val password = params[Users.PASSWORD]

    val validCurrentPassword = database.users.testPassword(id, currentPassword ?: "")
    val response = database.transactionWithResult<HttpStatusCode> {
        // Update name, if provided.
        name?.let { name ->
            database.users.updateName(id = id, name = name)
        }

        // Update email, if provided and current password is valid.
        email?.let { email ->
            if (validCurrentPassword) {
                if (validateEmail(email)) {
                    database.users.updateEmail(id = id, email = email)
                } else {
                    rollback(HttpStatusCode.BadRequest)
                }
            } else {
                rollback(HttpStatusCode.Forbidden)
            }
        }

        // Update password, if provided and current password is valid.
        password?.let { password ->
            if (validCurrentPassword) {
                if (validatePassword(password)) {
                    database.users.updatePassword(
                        id = id,
                        password = Argon2Password.encode(password)
                    )
                } else {
                    rollback(HttpStatusCode.BadRequest)
                }
            } else {
                rollback(HttpStatusCode.Forbidden)
            }
        }

        // If we're here and no rollbacks happened, the request was processed successfully.
        HttpStatusCode.NoContent
    }
    call.respond(response)
}

/**
 * Delete a user.
 */
private fun Route.deleteUser() = delete<Users.ById> { (_, id) ->
    authorizeForUser(id = id)

    val params = call.receiveParameters()
    val currentPassword = params.getOrFail(Users.CURRENT_PASSWORD)

    if (database.users.testPassword(id, currentPassword)) {
        database.users.delete(id = id)
        call.sessions.clear<Session>()
        call.respond(HttpStatusCode.OK)
    } else {
        call.respond(HttpStatusCode.Forbidden)
    }
}

/**
 * Tests the password for user with [id].
 */
private suspend fun UserQueries.testPassword(
    id: Long,
    password: String
): Boolean {
    val encoded = selectPasswordById(id = id).executeAsOneOrNull()
    if (encoded != null && Argon2Password.matches(password, encoded)) {
        return true
    }
    // Waste time to help mitigate timing attacks.
    @Suppress("MagicNumber")
    delay(Random.nextLong(200, 400))
    return false
}
