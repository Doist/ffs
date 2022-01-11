package doist.ffs.routes

import doist.ffs.auth.Permission
import doist.ffs.db.Project
import doist.ffs.db.SelectByProject
import doist.ffs.db.TokenGenerator
import doist.ffs.ext.bodyAsJson
import doist.ffs.ext.setBodyForm
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import routes.PATH_TOKEN
import routes.PATH_TOKENS
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ProjectRoutesTest {
    @Test
    fun testCreate() = testApplication {
        val client = createUserClient()
        val organizationId = client.withOrganization()
        val createResponse = client.client.post(
            "${PATH_ORGANIZATION(organizationId)}$PATH_PROJECTS"
        ) {
            setBodyForm("name" to "Test")
        }
        assert(createResponse.status == HttpStatusCode.Created)
        val resource = createResponse.headers[HttpHeaders.Location]
        assert(resource != null)

        val project = client.client.get(resource!!).bodyAsJson<Project>()
        assert(project.name == "Test")
    }

    @Test
    fun testGet() = testApplication {
        val client = createUserClient()
        val organizationId = client.withOrganization()
        val ids = List(3) { client.withProject(organizationId) }

        val projects = client.client
            .get("${PATH_ORGANIZATION(organizationId)}$PATH_PROJECTS")
            .bodyAsJson<List<Project>>()
        assert(ids.size == projects.size)
        assert(ids.toSet() == projects.map { it.id }.toSet())
    }

    @Test
    fun testGetNonexistentId() = testApplication {
        val client = createUserClient()
        assertFailsWith<ClientRequestException> {
            client.client.get(PATH_PROJECT(42))
        }
        assertFailsWith<ClientRequestException> {
            client.client.delete(PATH_PROJECT(42))
        }
    }

    @Test
    fun testUpdate() = testApplication {
        val client = createUserClient()
        val organizationId = client.withOrganization()

        val id = client.withProject(organizationId)
        var project = client.client.get(PATH_PROJECT(id)).bodyAsJson<Project>()

        val name = "${project.name} updated"
        client.client.put(PATH_PROJECT(id)) {
            setBodyForm("name" to name)
        }

        project = client.client.get(PATH_PROJECT(id)).bodyAsJson()
        assert(project.name == name)
    }

    @Test
    fun testUpdateDuplicateName() = testApplication {
        val client = createUserClient()
        val organizationId = client.withOrganization()
        val projectId = client.withProject(organizationId)
        val project = client.client.get(PATH_PROJECT(projectId)).bodyAsJson<Project>()
        assertFailsWith<ClientRequestException> {
            val id = client.withProject(organizationId)
            client.client.put("${PATH_ORGANIZATION(id)}$PATH_USERS") {
                setBodyForm("name" to project.name)
            }
        }
    }

    @Test
    fun testDelete() = testApplication {
        val client = createUserClient()
        val id = client.withProject(client.withOrganization())

        client.client.delete(PATH_PROJECT(id))

        assertFailsWith<ClientRequestException> {
            client.client.get(PATH_PROJECT(id)).bodyAsJson<Project?>()
        }
    }

    @Test
    fun testTokenCreate() = testApplication {
        val client = createUserClient()
        val id = client.withProject(client.withOrganization())

        val evalTokenValue = client.client.post("${PATH_PROJECT(id)}$PATH_TOKENS") {
            setBodyForm("permission" to Permission.EVAL, "description" to "Eval")
        }.bodyAsText()
        assert(TokenGenerator.isFormatValid(evalTokenValue))

        val readTokenValue = client.client.post("${PATH_PROJECT(id)}$PATH_TOKENS") {
            setBodyForm("permission" to Permission.READ, "description" to "Read")
        }.bodyAsText()
        assert(TokenGenerator.isFormatValid(readTokenValue))

        val tokens = client.client
            .get("${PATH_PROJECT(id)}$PATH_TOKENS")
            .bodyAsJson<List<SelectByProject>>()
        assert(tokens.size == 2)
        assert(tokens.all { it.project_id == id })
        assert(tokens.map { it.description }.toSet() == setOf("Eval", "Read"))
    }

    @Test
    fun testTokenCreateInvalidPermission() = testApplication {
        val client = createUserClient()
        val id = client.withProject(client.withOrganization())

        Permission.values().filter { it != Permission.EVAL && it != Permission.READ }.forEach {
            assertFailsWith<ClientRequestException> {
                client.client.post("${PATH_PROJECT(id)}$PATH_TOKENS") {
                    setBodyForm("permission" to it, "description" to it.name)
                }
            }
        }
    }

    @Test
    fun testTokenUpdate() = testApplication {
        val client = createUserClient()
        val id = client.withProject(client.withOrganization())

        client.client.post("${PATH_PROJECT(id)}$PATH_TOKENS") {
            setBodyForm("permission" to Permission.EVAL, "description" to "Eval")
        }.bodyAsText()

        var tokens = client.client
            .get("${PATH_PROJECT(id)}$PATH_TOKENS")
            .bodyAsJson<List<SelectByProject>>()
        client.client.put(PATH_TOKEN(tokens[0].id)) {
            setBodyForm("description" to "test")
        }

        tokens = client.client.get("${PATH_PROJECT(id)}$PATH_TOKENS").bodyAsJson()
        assert(tokens[0].description == "test")
    }

    @Test
    fun testTokenUpdatePermissionDoes() = testApplication {
        val client = createUserClient()
        val id = client.withProject(client.withOrganization())

        client.client.post("${PATH_PROJECT(id)}$PATH_TOKENS") {
            setBodyForm("permission" to Permission.EVAL, "description" to "Eval")
        }.bodyAsText()

        var tokens = client.client
            .get("${PATH_PROJECT(id)}$PATH_TOKENS")
            .bodyAsJson<List<SelectByProject>>()
        client.client.put(PATH_TOKEN(tokens[0].id)) {
            setBodyForm("description" to "test")
        }

        tokens = client.client.get("${PATH_PROJECT(id)}$PATH_TOKENS").bodyAsJson()
        assert(tokens[0].description == "test")
    }

    @Test
    fun testTokenDelete() = testApplication {
        val client = createUserClient()
        val id = client.withProject(client.withOrganization())

        client.client.post("${PATH_PROJECT(id)}$PATH_TOKENS") {
            setBodyForm("permission" to Permission.READ, "description" to "Read")
        }.bodyAsText()

        var tokens = client.client
            .get("${PATH_PROJECT(id)}$PATH_TOKENS")
            .bodyAsJson<List<SelectByProject>>()
        client.client.delete(PATH_TOKEN(tokens[0].id))

        tokens = client.client.get("${PATH_PROJECT(id)}$PATH_TOKENS").bodyAsJson()
        assert(tokens.isEmpty())
    }

    @Test
    fun testUnauthenticatedAccess() = testApplication {
        val client = createClient {
            install(HttpCookies)
            followRedirects = false
        }
        val id = createUserClient().run {
            val organizationId = withOrganization()
            withProject(organizationId)
        }
        assertFailsWith<RedirectResponseException> {
            client.get(PATH_PROJECT(id))
        }
        assertFailsWith<RedirectResponseException> {
            client.put(PATH_PROJECT(id)) {
                setBodyForm("name" to "Test")
            }
        }
        assertFailsWith<RedirectResponseException> {
            client.delete(PATH_PROJECT(id))
        }
        assertFailsWith<RedirectResponseException> {
            client.post("${PATH_PROJECT(id)}$PATH_TOKENS") {
                setBodyForm("permission" to Permission.EVAL, "description" to "Eval")
            }
        }
    }
}
