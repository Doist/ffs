package doist.ffs.routes

import doist.ffs.auth.Argon2Password
import doist.ffs.db.SelectById
import doist.ffs.db.capturingLastInsertId
import doist.ffs.db.users
import doist.ffs.module
import doist.ffs.plugins.database
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.testing.withTestApplication
import kotlin.test.Test

class UserRoutesTest {
    @Test
    fun testUserRegister() = withTestApplication(Application::module) {
        assertResourceCreates(
            uri = "$PATH_USERS/$PATH_REGISTER",
            args = listOf("name" to NAME, "email" to EMAIL, "password" to PASSWORD)
        )
        val user = application.database.users.selectByEmail(EMAIL).executeAsOne()
        assert(user.name == NAME)
    }

    @Test
    fun testUserRegisterInvalidEmail() = withTestApplication(Application::module) {
        assertStatus(
            uri = "$PATH_USERS/$PATH_REGISTER",
            method = HttpMethod.Post,
            args = listOf("name" to NAME, "email" to "goncalo@doist.c", "password" to PASSWORD),
            status = HttpStatusCode.BadRequest
        )
    }

    @Test
    fun testUserRegisterInvalidPassword() = withTestApplication(Application::module) {
        assertStatus(
            uri = "$PATH_USERS/$PATH_REGISTER",
            method = HttpMethod.Post,
            args = listOf("name" to NAME, "email" to EMAIL, "password" to "1234567"),
            status = HttpStatusCode.BadRequest
        )
    }

    @Test
    fun testUserLogin() = withTestApplication(Application::module) {
        application.database.users.insert(
            name = NAME,
            email = EMAIL,
            password = Argon2Password.encode(PASSWORD)
        )
        assertResource<SelectById>(
            uri = "$PATH_USERS$PATH_LOGIN",
            method = HttpMethod.Post,
            args = listOf("email" to EMAIL, "password" to PASSWORD),
        ) { user ->
            assert(user.name == NAME)
            assert(user.email == EMAIL)
        }
    }

    @Test
    fun testUserLoginInvalid() = withTestApplication(Application::module) {
        application.database.users.insert(
            name = NAME,
            email = EMAIL,
            password = Argon2Password.encode(PASSWORD)
        )
        assertStatus(
            uri = "$PATH_USERS$PATH_LOGIN",
            method = HttpMethod.Post,
            args = listOf("email" to EMAIL, "password" to "124567"),
            status = HttpStatusCode.Unauthorized
        )
    }

    @Test
    fun testUserUpdate() = withTestApplication(Application::module) {
        val id = application.database.capturingLastInsertId {
            users.insert(name = NAME, email = EMAIL, password = Argon2Password.encode(PASSWORD))
        }
        assertResourceUpdates(
            uri = PATH_USER(id),
            args = listOf("name" to NAME_UDPATED)
        )
        var user = application.database.users.selectById(id).executeAsOne()
        assert(user.name == NAME_UDPATED)
        assertResourceUpdates(
            uri = PATH_USER(id),
            args = listOf("email" to EMAIL_UPDATED, "current_password" to PASSWORD)
        )
        user = application.database.users.selectById(id).executeAsOne()
        assert(user.email == EMAIL_UPDATED)
        assertResourceUpdates(
            uri = PATH_USER(id),
            args = listOf("password" to PASSWORD_UPDATED, "current_password" to PASSWORD)
        )
    }

    @Test
    fun testUserUpdateInvalidEmail() = withTestApplication(Application::module) {
        val id = application.database.capturingLastInsertId {
            users.insert(name = NAME, email = EMAIL, password = Argon2Password.encode(PASSWORD))
        }
        assertStatus(
            uri = PATH_USER(id),
            method = HttpMethod.Put,
            args = listOf("email" to "goncalo@doist.c", "current_password" to PASSWORD),
            status = HttpStatusCode.BadRequest
        )
    }

    @Test
    fun testUserUpdateInvalidCurrentPassword() = withTestApplication(Application::module) {
        val id = application.database.capturingLastInsertId {
            users.insert(name = NAME, email = EMAIL, password = Argon2Password.encode(PASSWORD))
        }
        assertStatus(
            uri = PATH_USER(id),
            method = HttpMethod.Put,
            status = HttpStatusCode.UnsupportedMediaType
        )
        assertStatus(
            uri = PATH_USER(id),
            method = HttpMethod.Put,
            args = listOf("email" to EMAIL_UPDATED),
            status = HttpStatusCode.Forbidden
        )
        var user = application.database.users.select(id).executeAsOne()
        assert(user.email != EMAIL_UPDATED)
        assertStatus(
            uri = PATH_USER(id),
            method = HttpMethod.Put,
            args = listOf("email" to EMAIL_UPDATED, "current_password" to PASSWORD_UPDATED),
            status = HttpStatusCode.Forbidden
        )
        user = application.database.users.select(id).executeAsOne()
        assert(user.email != EMAIL_UPDATED)
        assertStatus(
            uri = PATH_USER(id),
            method = HttpMethod.Put,
            args = listOf("password" to PASSWORD_UPDATED),
            status = HttpStatusCode.Forbidden
        )
        assertStatus(
            uri = PATH_USER(id),
            method = HttpMethod.Put,
            args = listOf("password" to PASSWORD_UPDATED, "current_password" to PASSWORD_UPDATED),
            status = HttpStatusCode.Forbidden
        )
    }

    @Test
    fun testUserDelete() = withTestApplication(Application::module) {
        val id = application.database.capturingLastInsertId {
            users.insert(name = NAME, email = EMAIL, password = Argon2Password.encode(PASSWORD))
        }
        assertResourceDeletes(
            uri = PATH_USER(id),
            args = listOf("current_password" to PASSWORD),
        )
        val user = application.database.users.select(id).executeAsOneOrNull()
        assert(user == null)
    }

    @Test
    fun testUserDeleteInvalidCurrentPassword() = withTestApplication(Application::module) {
        val id = application.database.capturingLastInsertId {
            users.insert(name = NAME, email = EMAIL, password = Argon2Password.encode(PASSWORD))
        }
        assertStatus(
            uri = PATH_USER(id),
            method = HttpMethod.Delete,
            status = HttpStatusCode.UnsupportedMediaType
        )
        var user = application.database.users.select(id).executeAsOneOrNull()
        assert(user != null)
        assertStatus(
            uri = PATH_USER(id),
            method = HttpMethod.Delete,
            args = listOf("current_password" to PASSWORD_UPDATED),
            status = HttpStatusCode.Forbidden
        )
        user = application.database.users.select(id).executeAsOneOrNull()
        assert(user != null)
    }

    companion object {
        private const val NAME = "Gonçalo"
        private const val NAME_UDPATED = "Gonçalo Silva"
        private const val EMAIL = "goncalo@doist.com"
        private const val EMAIL_UPDATED = "goncalo@doist.io"
        private const val PASSWORD = "password123"
        private const val PASSWORD_UPDATED = "password1234"
    }
}
