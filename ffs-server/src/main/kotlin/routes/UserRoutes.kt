package doist.ffs.routes

import doist.ffs.auth.Argon2Password
import doist.ffs.auth.Session
import doist.ffs.db.UserQueries
import doist.ffs.db.capturingLastInsertId
import doist.ffs.db.users
import doist.ffs.ext.authorizeForUser
import doist.ffs.ext.optionalRoute
import doist.ffs.plugins.database
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import io.ktor.server.util.getOrFail
import kotlinx.coroutines.delay
import routes.PATH_LATEST
import kotlin.random.Random

const val PATH_USERS = "/users"
const val PATH_REGISTER = "/register"
const val PATH_LOGIN = "/login"

@Suppress("FunctionName")
fun PATH_USER(id: Any) = "$PATH_USERS/$id"

fun Application.installUserRoutes() = routing {
    optionalRoute(PATH_LATEST) {
        route(PATH_USERS) {
            registerUser()
            loginUser()

            authenticate("session") {
                updateUser()
                deleteUser()
            }
        }
    }
}

/**
 * Register a user.
 *
 * On success, responds `201 Created` with an empty body.
 *
 * | Parameter  | Required | Description           |
 * | ---------- | -------- | --------------------- |
 * | `name`     | Yes      | Name of the user.     |
 * | `email`    | Yes      | Email of the user.    |
 * | `password` | Yes      | Password of the user. |
 */
private fun Route.registerUser() = post(PATH_REGISTER) {
    val params = call.receiveParameters()
    val name = params.getOrFail("name")
    val email = params.getOrFail("email")
    val password = params.getOrFail("password")

    if (validateEmail(email) && validatePassword(password)) {
        val id = database.capturingLastInsertId {
            users.insert(name = name, email = email, password = Argon2Password.encode(password))
        }
        call.sessions.set(Session(id))
        call.response.header(HttpHeaders.Location, PATH_USER(id))
        call.respond(HttpStatusCode.Created)
    } else {
        call.respond(HttpStatusCode.BadRequest)
    }
}

/**
 * Log in a user.
 *
 * On success, responds `200 OK` with the user data.
 *
 * | Parameter  | Required | Description           |
 * | ---------- | -------- | --------------------- |
 * | `email`    | Yes      | Email of the user.    |
 * | `password` | Yes      | Password of the user. |
 */
private fun Route.loginUser() = post(PATH_LOGIN) {
    val params = call.receiveParameters()
    val email = params.getOrFail("email")
    val password = params.getOrFail("password")

    val id = database.users.selectIdByEmail(email = email).executeAsOneOrNull()
    if (id != null && database.users.testPassword(id, password)) {
        call.sessions.set(Session(id))
        call.respond(HttpStatusCode.OK, database.users.selectById(id = id).executeAsOne())
        return@post
    } else {
        call.respond(HttpStatusCode.Unauthorized)
    }
}

/**
 * Update a user.
 *
 * On success, responds `204 No Content` with an empty body.
 *
 * | Parameter          | Required                              | Description               |
 * | ------------------ | ------------------------------------- | ------------------------- |
 * | `name`             | No                                    | Name of the user.         |
 * | `email`            | No                                    | Email of the user.        |
 * | `password`         | No                                    | Password of the user.     |
 * | `current_password` | If `email` or `password are provided  | Current user password.    |
 */
private fun Route.updateUser() = put("{id}") {
    val id = call.parameters.getOrFail<Long>("id")
    val params = call.receiveParameters()
    val currentPassword = params["current_password"]
    val name = params["name"]
    val email = params["email"]
    val password = params["password"]

    authorizeForUser(id = id)

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
 *
 * On success, responds `204 No Content` with an empty body.
 *
 * | Parameter          | Required  | Description             |
 * | ------------------ | --------- | ----------------------- |
 * | `current_password` | Yes       | Current user password.  |
 */
private fun Route.deleteUser() = delete("{id}") {
    val id = call.parameters.getOrFail<Long>("id")
    val params = call.receiveParameters()
    val currentPassword = params.getOrFail("current_password")

    authorizeForUser(id = id)

    if (database.users.testPassword(id, currentPassword)) {
        database.users.delete(id = id)
        call.respond(HttpStatusCode.NoContent)
    } else {
        call.respond(HttpStatusCode.Forbidden)
    }
}

/**
 * Validates the email.
 */
private val EMAIL_REGEXP = (
    "^(?=.{1,64}@)[\\p{L}0-9_-]+([\\.+][\\p{L}0-9_-]+)*" +
        "@[^-][\\p{L}0-9-]+(\\.[\\p{L}0-9-]+)*" +
        "(\\.[\\p{L}]{2,})\$"
    ).toRegex()
private fun validateEmail(email: String): Boolean {
    return email.matches(EMAIL_REGEXP)
}

/**
 * Validates the password.
 */
private val PASSWORD_REGEXP = ".{8,}".toRegex()
private fun validatePassword(password: String): Boolean {
    return password.matches(PASSWORD_REGEXP)
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
