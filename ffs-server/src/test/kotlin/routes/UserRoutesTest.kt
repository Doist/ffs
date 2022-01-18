package doist.ffs.routes

import doist.ffs.db.SelectById
import doist.ffs.endpoints.Users
import doist.ffs.ext.bodyAsJson
import doist.ffs.ext.setBodyForm
import doist.ffs.plugins.SessionHeader
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.plugins.resources.delete
import io.ktor.client.plugins.resources.href
import io.ktor.client.plugins.resources.post
import io.ktor.client.plugins.resources.put
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import routes.PATH_LATEST
import kotlin.test.Test
import kotlin.test.assertFailsWith

class UserRoutesTest {
    @Test
    fun registerLogin() = testApplication {
        val client = createLocalClient()

        // Register a user and verify it was created.
        val registerResponse = client.post(Users.Register()) {
            setBodyForm(
                Users.NAME to "Test",
                Users.EMAIL to "test@test.test",
                Users.PASSWORD to "password123"
            )
        }
        assert(registerResponse.status == HttpStatusCode.Created)

        // Login as the user and verify the details match.
        val loginResponse = client.post(Users.Login()) {
            setBodyForm(
                Users.EMAIL to "test@test.test",
                Users.PASSWORD to "password123"
            )
        }
        assert(loginResponse.status == HttpStatusCode.OK)
        val user = loginResponse.bodyAsJson<SelectById>()
        assert(user.name == "Test")
        assert(user.email == "test@test.test")
    }

    @Test
    fun registerInvalidEmail() = testApplication {
        val client = createLocalClient()

        assertFailsWith<ClientRequestException> {
            client.post(Users.Register()) {
                setBodyForm(
                    Users.NAME to "Test",
                    Users.EMAIL to "no-email",
                    Users.PASSWORD to "password123"
                )
            }
        }
    }

    @Test
    fun registerInvalidPassword() = testApplication {
        val client = createLocalClient()

        assertFailsWith<ClientRequestException> {
            client.post(Users.Register()) {
                setBodyForm(
                    Users.NAME to "Test",
                    Users.EMAIL to "test@test.test",
                    Users.PASSWORD to "1234567"
                )
            }
        }
    }

    @Test
    fun logout() = testApplication {
        val client = createLocalClient()
        client.post(Users.Register()) {
            setBodyForm(
                Users.NAME to "Test",
                Users.EMAIL to "test@test.test",
                Users.PASSWORD to "password123"
            )
        }

        val logoutResponse = client.post(Users.Logout())
        assert(logoutResponse.status == HttpStatusCode.OK)
    }

    @Test
    fun update() = testApplication {
        val client = createLocalClient()
        val id = client.post(Users.Register()) {
            setBodyForm(
                Users.NAME to "Test User",
                Users.EMAIL to "test@test.test",
                Users.PASSWORD to "password123"
            )
        }.headers[HttpHeaders.Location]!!.substringAfterLast('/').toLong()

        // Update various user details.
        client.put(Users.ById(id = id)) {
            setBodyForm(Users.NAME to "Test")
        }
        client.put(Users.ById(id = id)) {
            setBodyForm(Users.EMAIL to "test@test.com", Users.CURRENT_PASSWORD to "password123")
        }
        client.put(Users.ById(id = id)) {
            setBodyForm(Users.NAME to "User Test", Users.CURRENT_PASSWORD to "password123")
        }

        // Login to obtain user info again, and verify it.
        val loginResponse = client.post(Users.Login()) {
            setBodyForm(Users.EMAIL to "test@test.com", Users.PASSWORD to "password123")
        }
        val user = loginResponse.bodyAsJson<SelectById>()
        assert(user.name == "User Test")
        assert(user.email == "test@test.com")
    }

    @Test
    fun updateEmailInvalid() = testApplication {
        val client = createLocalClient()
        val id = client.post(Users.Register()) {
            setBodyForm(
                Users.NAME to "Test",
                Users.EMAIL to "test@test.test",
                Users.PASSWORD to "password123"
            )
        }.headers[HttpHeaders.Location]!!.substringAfterLast('/').toLong()

        listOf("test@test.t", "test@127.0.0.1", "@test.test").forEach { email ->
            assertFailsWith<ClientRequestException> {
                client.put(Users.ById(id = id)) {
                    setBodyForm("email" to email, Users.CURRENT_PASSWORD to "password123")
                }
            }
        }
    }

    @Test
    fun updateCurrentPasswordInvalid() = testApplication {
        val client = createLocalClient()
        val id = client.post(Users.Register()) {
            setBodyForm(
                Users.NAME to "Test",
                Users.EMAIL to "test@test.test",
                Users.PASSWORD to "password123"
            )
        }.headers[HttpHeaders.Location]!!.substringAfterLast('/').toLong()

        assertFailsWith<ClientRequestException> {
            client.put(Users.ById(id = id)) {
                setBodyForm(Users.EMAIL to "test@test.com")
            }
        }
        assertFailsWith<ClientRequestException> {
            client.put(Users.ById(id = id)) {
                setBodyForm(
                    Users.EMAIL to "test@test.com",
                    Users.CURRENT_PASSWORD to "wrongpassword"
                )
            }
        }
        assertFailsWith<ClientRequestException> {
            client.put(Users.ById(id = id)) {
                setBodyForm(Users.PASSWORD to "newpassword")
            }
        }
        assertFailsWith<ClientRequestException> {
            client.put(Users.ById(id = id)) {
                setBodyForm(
                    Users.PASSWORD to "newpassword",
                    Users.CURRENT_PASSWORD to "wrongpassword"
                )
            }
        }
    }

    @Test
    fun delete() = testApplication {
        val client = createLocalClient()
        val id = client.post(Users.Register()) {
            setBodyForm(
                Users.NAME to "Test",
                Users.EMAIL to "test@test.test",
                Users.PASSWORD to "password123"
            )
        }.headers[HttpHeaders.Location]!!.substringAfterLast('/').toLong()

        val deleteResponse = client.delete(Users.ById(id = id)) {
            setBodyForm(Users.CURRENT_PASSWORD to "password123")
        }
        assert(deleteResponse.status == HttpStatusCode.OK)

        // Ensure one can't login as a deleted user.
        assertFailsWith<ClientRequestException> {
            client.post(Users.Login()) {
                setBodyForm(Users.EMAIL to "test@test.test", Users.PASSWORD to "password123")
            }
        }
    }

    @Test
    fun deleteCurrentPasswordInvalid() = testApplication {
        val client = createLocalClient()
        val id = client.post(Users.Register()) {
            setBodyForm(
                Users.NAME to "Test",
                Users.EMAIL to "test@test.test",
                Users.PASSWORD to "password123"
            )
        }.headers[HttpHeaders.Location]!!.substringAfterLast('/').toLong()

        assertFailsWith<ClientRequestException> {
            client.delete(Users.ById(id = id)) {
                setBodyForm(Users.CURRENT_PASSWORD to "wrongpassword")
            }
        }
    }

    @Test
    fun apiLatestOptional() = testApplication {
        val client = createUserClient()
        val versions = listOf("", PATH_LATEST)

        val registerResponse = versions.map { version ->
            client.client.post("$version${client.client.href(Users.Register())}") {
                setBodyForm(
                    Users.NAME to "Test $version",
                    Users.EMAIL to "test${version.trim { !it.isLetterOrDigit() } }@test.test",
                    Users.PASSWORD to "password123"
                )
            }
        }
        assert(registerResponse[0].status == registerResponse[1].status)

        val ids = registerResponse.map {
            it.headers[HttpHeaders.Location]!!.substringAfterLast('/').toLong()
        }
        val updateResponses = versions.zip(ids).map { (version, id) ->
            client.client.post("$version${client.client.href(Users.Login())}") {
                setBodyForm(
                    Users.EMAIL to "test${version.trim { !it.isLetterOrDigit() } }@test.test",
                    Users.PASSWORD to "password123"
                )
            }

            client.client.put("$version${client.client.href(Users.ById(id = id))}") {
                setBodyForm(Users.NAME to "Test $version updated")
            }
        }
        assert(updateResponses[0].status == updateResponses[1].status)
    }

    private fun ApplicationTestBuilder.createLocalClient() = createClient {
        install(Resources)
        install(SessionHeader) {
            name = HttpHeaders.Authorization
        }
    }
}
