package doist.ffs.routes

import doist.ffs.db.SelectById
import doist.ffs.ext.bodyAsJson
import doist.ffs.ext.setBodyForm
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import routes.PATH_LATEST
import kotlin.test.Test
import kotlin.test.assertFailsWith

class UserRoutesTest {
    @Test
    fun registerLogin() = testApplication {
        val client = createClient { }

        // Register a user and verify it was created.
        val registerResponse = client.post("$PATH_USERS$PATH_REGISTER") {
            setBodyForm(
                "name" to "Test",
                "email" to "test@test.test",
                "password" to "password123"
            )
        }
        assert(registerResponse.status == HttpStatusCode.Created)

        // Login as the user and verify the details match.
        val loginResponse = client.post("$PATH_USERS$PATH_LOGIN") {
            setBodyForm("email" to "test@test.test", "password" to "password123")
        }
        assert(loginResponse.status == HttpStatusCode.OK)
        val user = loginResponse.bodyAsJson<SelectById>()
        assert(user.name == "Test")
        assert(user.email == "test@test.test")
    }

    @Test
    fun registerInvalidEmail() = testApplication {
        assertFailsWith<ClientRequestException> {
            client.post("$PATH_USERS$PATH_REGISTER") {
                setBodyForm("name" to "Test", "email" to "no-email", "password" to "password123")
            }
        }
    }

    @Test
    fun registerInvalidPassword() = testApplication {
        assertFailsWith<ClientRequestException> {
            client.post("$PATH_USERS$PATH_REGISTER") {
                setBodyForm(
                    "name" to "Test",
                    "email" to "test@test.test",
                    "password" to "1234567"
                )
            }
        }
    }

    @Test
    fun logout() = testApplication {
        val client = createClient {
            install(HttpCookies)
            expectSuccess = false
        }
        val id = client.post("$PATH_USERS$PATH_REGISTER") {
            setBodyForm(
                "name" to "Test",
                "email" to "test@test.test",
                "password" to "password123"
            )
        }.headers[HttpHeaders.Location]!!.substringAfterLast('/')

        // Verify logout redirects.
        assert(client.post("$PATH_USERS$PATH_LOGOUT").status == HttpStatusCode.Found)
    }

    @Test
    fun update() = testApplication {
        val client = createClient {
            install(HttpCookies)
        }
        val id = client.post("$PATH_USERS$PATH_REGISTER") {
            setBodyForm(
                "name" to "Test User",
                "email" to "test@test.test",
                "password" to "password123"
            )
        }.headers[HttpHeaders.Location]!!.substringAfterLast('/')

        // Update various user details.
        client.put(PATH_USER(id)) {
            setBodyForm("name" to "Test")
        }
        client.put(PATH_USER(id)) {
            setBodyForm("email" to "test@test.com", "current_password" to "password123")
        }
        client.put(PATH_USER(id)) {
            setBodyForm("name" to "User Test", "current_password" to "password123")
        }

        // Login to obtain user info again, and verify it.
        val loginResponse = client.post("$PATH_USERS$PATH_LOGIN") {
            setBodyForm("email" to "test@test.com", "password" to "password123")
        }
        val user = loginResponse.bodyAsJson<SelectById>()
        assert(user.name == "User Test")
        assert(user.email == "test@test.com")
    }

    @Test
    fun updateEmailInvalid() = testApplication {
        val client = createClient {
            install(HttpCookies)
        }
        val id = client.post("$PATH_USERS$PATH_REGISTER") {
            setBodyForm(
                "name" to "Test",
                "email" to "test@test.test",
                "password" to "password123"
            )
        }.headers[HttpHeaders.Location]!!.substringAfterLast('/')

        listOf("test@test.t", "test@127.0.0.1", "@test.test").forEach { email ->
            assertFailsWith<ClientRequestException> {
                client.put(PATH_USER(id)) {
                    setBodyForm("email" to email, "current_password" to "password123")
                }
            }
        }
    }

    @Test
    fun updateCurrentPasswordInvalid() = testApplication {
        val client = createClient {
            install(HttpCookies)
        }
        val id = client.post("$PATH_USERS$PATH_REGISTER") {
            setBodyForm(
                "name" to "Test",
                "email" to "test@test.test",
                "password" to "password123"
            )
        }.headers[HttpHeaders.Location]!!.substringAfterLast('/')

        assertFailsWith<ClientRequestException> {
            client.put(PATH_USER(id)) {
                setBodyForm("email" to "test@test.com")
            }
        }
        assertFailsWith<ClientRequestException> {
            client.put(PATH_USER(id)) {
                setBodyForm("email" to "test@test.com", "current_password" to "wrongpassword")
            }
        }
        assertFailsWith<ClientRequestException> {
            client.put(PATH_USER(id)) {
                setBodyForm("password" to "newpassword")
            }
        }
        assertFailsWith<ClientRequestException> {
            client.put(PATH_USER(id)) {
                setBodyForm("password" to "newpassword", "current_password" to "wrongpassword")
            }
        }
    }

    @Test
    fun delete() = testApplication {
        val client = createClient {
            install(HttpCookies)
        }
        val id = client.post("$PATH_USERS$PATH_REGISTER") {
            setBodyForm(
                "name" to "Test",
                "email" to "test@test.test",
                "password" to "password123"
            )
        }.headers[HttpHeaders.Location]!!.substringAfterLast('/')

        assertFailsWith<RedirectResponseException> {
            client.delete(PATH_USER(id)) {
                setBodyForm("current_password" to "password123")
            }
        }

        // Ensure one can't login as a deleted user.
        assertFailsWith<ClientRequestException> {
            client.post("$PATH_USERS$PATH_LOGIN") {
                setBodyForm("email" to "test@test.test", "password" to "password123")
            }
        }
    }

    @Test
    fun deleteCurrentPasswordInvalid() = testApplication {
        val client = createClient {
            install(HttpCookies)
        }
        val id = client.post("$PATH_USERS$PATH_REGISTER") {
            setBodyForm(
                "name" to "Test",
                "email" to "test@test.test",
                "password" to "password123"
            )
        }.headers[HttpHeaders.Location]!!.substringAfterLast('/')

        assertFailsWith<ClientRequestException> {
            client.delete(PATH_USER(id)) {
                setBodyForm("current_password" to "wrongpassword")
            }
        }
    }

    @Test
    fun apiLatestOptional() = testApplication {
        val client = createUserClient()
        val versions = listOf("", PATH_LATEST)

        val registerResponse = versions.map { version ->
            client.client.post("$version$PATH_USERS$PATH_REGISTER") {
                setBodyForm(
                    "name" to "Test $version",
                    "email" to "test${version.trim { !it.isLetterOrDigit() } }@test.test",
                    "password" to "password123"
                )
            }
        }
        assert(registerResponse[0].status == registerResponse[1].status)

        val ids = registerResponse.map {
            it.headers[HttpHeaders.Location]!!.substringAfterLast('/')
        }
        val updateResponses = versions.zip(ids).map { (version, id) ->
            client.client.post("$version$PATH_USERS$PATH_LOGIN") {
                setBodyForm(
                    "email" to "test${version.trim { !it.isLetterOrDigit() } }@test.test",
                    "password" to "password123"
                )
            }

            client.client.put("$version${PATH_USER(id)}") {
                setBodyForm("name" to "Test $version updated")
            }
        }
        assert(updateResponses[0].status == updateResponses[1].status)
    }
}
