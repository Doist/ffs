package doist.ffs.routes

import doist.ffs.db.Project
import doist.ffs.ext.bodyAsJson
import doist.ffs.ext.setBodyForm
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
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
    fun testDelete() = testApplication {
        val client = createUserClient()
        val id = client.withProject(client.withOrganization())

        client.client.delete(PATH_PROJECT(id))

        assertFailsWith<ClientRequestException> {
            client.client.get(PATH_PROJECT(id)).bodyAsJson<Project?>()
        }
    }

    @Test
    fun testIncorrectParams() = testApplication {
        val client = createUserClient()

        // Nonexistent id.
        assertFailsWith<ClientRequestException> {
            client.client.get(PATH_PROJECT(42))
        }
        assertFailsWith<ClientRequestException> {
            client.client.delete(PATH_PROJECT(42))
        }

        // Duplicate name.
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
    }
}
