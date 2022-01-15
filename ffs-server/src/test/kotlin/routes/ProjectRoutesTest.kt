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
import routes.PATH_LATEST
import routes.PATH_TOKEN
import routes.PATH_TOKENS
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ProjectRoutesTest {
    @Test
    fun create() = testApplication {
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
    fun get() = testApplication {
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
    fun getNonexistentId() = testApplication {
        val client = createUserClient()
        assertFailsWith<ClientRequestException> {
            client.client.get(PATH_PROJECT(42))
        }
        assertFailsWith<ClientRequestException> {
            client.client.delete(PATH_PROJECT(42))
        }
    }

    @Test
    fun update() = testApplication {
        val client = createUserClient()
        val id = client.withProject(client.withOrganization())

        var project = client.client.get(PATH_PROJECT(id)).bodyAsJson<Project>()
        val name = "${project.name} updated"
        client.client.put(PATH_PROJECT(id)) {
            setBodyForm("name" to name)
        }

        project = client.client.get(PATH_PROJECT(id)).bodyAsJson()
        assert(project.name == name)
    }

    @Test
    fun updateDuplicateName() = testApplication {
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
    fun delete() = testApplication {
        val client = createUserClient()
        val id = client.withProject(client.withOrganization())

        client.client.delete(PATH_PROJECT(id))

        assertFailsWith<ClientRequestException> {
            client.client.get(PATH_PROJECT(id)).bodyAsJson<Project?>()
        }
    }

    @Test
    fun tokenCreate() = testApplication {
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
    fun tokenCreateInvalidPermission() = testApplication {
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
    fun tokenUpdate() = testApplication {
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
    fun tokenUpdatePermissionDoes() = testApplication {
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
    fun tokenDelete() = testApplication {
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
    fun unauthenticatedAccess() = testApplication {
        val client = createClient {
            install(HttpCookies)
            followRedirects = false
        }
        val id = createUserClient().run {
            withProject(withOrganization())
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

    @Test
    fun apiLatestOptional() = testApplication {
        val client = createUserClient()
        val versions = listOf(PATH_LATEST, "")

        val createResponses = versions.map {
            client.client.post(
                "$it${PATH_ORGANIZATION(client.withOrganization())}$PATH_PROJECTS"
            ) {
                setBodyForm("name" to "Test")
            }
        }
        assert(createResponses[0].status == createResponses[1].status)

        val ids = createResponses.map {
            it.headers[HttpHeaders.Location]!!.substringAfterLast('/')
        }
        val updateResponses = versions.zip(ids).map { (version, id) ->
            client.client.put("$version${PATH_PROJECT(id)}") {
                setBodyForm("name" to "Test updated")
            }
        }
        assert(updateResponses[0].status == updateResponses[1].status)

        val deleteResponse = versions.zip(ids).map { (version, id) ->
            client.client.delete("$version${PATH_PROJECT(id)}")
        }
        assert(deleteResponse[0].status == deleteResponse[1].status)
    }
}
