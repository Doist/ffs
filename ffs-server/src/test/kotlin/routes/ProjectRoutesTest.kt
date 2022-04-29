package doist.ffs.routes

import doist.ffs.db.Permission
import doist.ffs.db.Project
import doist.ffs.db.SelectByProject
import doist.ffs.db.TokenGenerator
import doist.ffs.endpoints.Organizations
import doist.ffs.endpoints.Organizations.Companion.Projects
import doist.ffs.endpoints.Projects
import doist.ffs.endpoints.Projects.Companion.Tokens
import doist.ffs.endpoints.Tokens
import doist.ffs.ext.bodyAsJson
import doist.ffs.ext.setBodyForm
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.plugins.resources.delete
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.href
import io.ktor.client.plugins.resources.post
import io.ktor.client.plugins.resources.put
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import routes.PATH_LATEST
import kotlin.test.Test

class ProjectRoutesTest {
    @Test
    fun create() = testApplication {
        val client = createSessionClient()
        val organizationId = client.withOrganization()
        val createResponse = client.client.post(
            Organizations.ById.Projects(organizationId = organizationId),
        ) {
            setBodyForm(Projects.NAME to "Test")
        }
        assert(createResponse.status == HttpStatusCode.Created)
        val resource = createResponse.headers[HttpHeaders.Location]
        assert(resource != null)

        val project = client.client.get(resource!!).bodyAsJson<Project>()
        assert(project.name == "Test")
    }

    @Test
    fun get() = testApplication {
        val client = createSessionClient()
        val organizationId = client.withOrganization()
        val ids = List(3) { client.withProject(organizationId) }

        val projects = client.client
            .get(Organizations.ById.Projects(organizationId = organizationId))
            .bodyAsJson<List<Project>>()
        assert(ids.size == projects.size)
        assert(ids.toSet() == projects.map { it.id }.toSet())
    }

    @Test
    fun getNonexistentId() = testApplication {
        val client = createSessionClient()

        var response = client.client.get(Projects.ById(id = 42))
        assert(response.status == HttpStatusCode.Forbidden)

        response = client.client.delete(Projects.ById(id = 42))
        assert(response.status == HttpStatusCode.Forbidden)
    }

    @Test
    fun update() = testApplication {
        val client = createSessionClient()
        val id = client.withProject(client.withOrganization())

        var project = client.client.get(Projects.ById(id = id)).bodyAsJson<Project>()
        val name = "${project.name} updated"
        client.client.put(Projects.ById(id = id)) {
            setBodyForm(Projects.NAME to name)
        }

        project = client.client.get(Projects.ById(id = id)).bodyAsJson()
        assert(project.name == name)
    }

    @Test
    fun updateDuplicateName() = testApplication {
        val client = createSessionClient()
        val organizationId = client.withOrganization()
        val projectId = client.withProject(organizationId)
        val project = client.client.get(Projects.ById(id = projectId)).bodyAsJson<Project>()
        val id = client.withProject(organizationId)

        val response = client.client.put(Projects.ById(id = id)) {
            setBodyForm(Projects.NAME to project.name)
        }
        assert(response.status == HttpStatusCode.BadRequest)
    }

    @Test
    fun delete() = testApplication {
        val client = createSessionClient()
        val id = client.withProject(client.withOrganization())

        client.client.delete(Projects.ById(id = id))

        val response = client.client.get(Projects.ById(id = id))
        assert(response.status == HttpStatusCode.Forbidden)
    }

    @Test
    fun tokenCreate() = testApplication {
        val client = createSessionClient()
        val id = client.withProject(client.withOrganization())

        val evalTokenValue = client.client.post(Projects.ById.Tokens(projectId = id)) {
            setBodyForm(
                Tokens.PERMISSION to Permission.EVAL,
                Tokens.DESCRIPTION to "Eval"
            )
        }.bodyAsText()
        assert(TokenGenerator.isFormatValid(evalTokenValue))

        val readTokenValue = client.client.post(Projects.ById.Tokens(projectId = id)) {
            setBodyForm(
                Tokens.PERMISSION to Permission.READ,
                Tokens.DESCRIPTION to "Read"
            )
        }.bodyAsText()
        assert(TokenGenerator.isFormatValid(readTokenValue))

        val tokens = client.client
            .get(Projects.ById.Tokens(projectId = id))
            .bodyAsJson<List<SelectByProject>>()
        assert(tokens.size == 2)
        assert(tokens.all { it.project_id == id })
        assert(tokens.map { it.description }.toSet() == setOf("Eval", "Read"))
    }

    @Test
    fun tokenCreateInvalidPermission() = testApplication {
        val client = createSessionClient()
        val id = client.withProject(client.withOrganization())

        Permission.values().filter { it != Permission.EVAL && it != Permission.READ }.forEach {
            val response = client.client.post(Projects.ById.Tokens(projectId = id)) {
                setBodyForm(
                    Tokens.PERMISSION to it,
                    Tokens.DESCRIPTION to it.name
                )
            }
            assert(response.status == HttpStatusCode.BadRequest)
        }
    }

    @Test
    fun tokenUpdate() = testApplication {
        val client = createSessionClient()
        val id = client.withProject(client.withOrganization())

        client.client.post(Projects.ById.Tokens(projectId = id)) {
            setBodyForm(
                Tokens.PERMISSION to Permission.EVAL,
                Tokens.DESCRIPTION to "Eval"
            )
        }.bodyAsText()

        var tokens = client.client
            .get(Projects.ById.Tokens(projectId = id))
            .bodyAsJson<List<SelectByProject>>()
        client.client.put(Tokens.ById(id = tokens[0].id)) {
            setBodyForm(Tokens.DESCRIPTION to "test")
        }

        tokens = client.client.get(Projects.ById.Tokens(projectId = id)).bodyAsJson()
        assert(tokens[0].description == "test")
    }

    @Test
    fun tokenUpdatePermissionDoes() = testApplication {
        val client = createSessionClient()
        val id = client.withProject(client.withOrganization())

        client.client.post(Projects.ById.Tokens(projectId = id)) {
            setBodyForm(
                Tokens.PERMISSION to Permission.EVAL,
                Tokens.DESCRIPTION to "Eval"
            )
        }.bodyAsText()

        var tokens = client.client
            .get(Projects.ById.Tokens(projectId = id))
            .bodyAsJson<List<SelectByProject>>()
        client.client.put(Tokens.ById(id = tokens[0].id)) {
            setBodyForm(Tokens.DESCRIPTION to "test")
        }

        tokens = client.client.get(Projects.ById.Tokens(projectId = id)).bodyAsJson()
        assert(tokens[0].description == "test")
    }

    @Test
    fun tokenDelete() = testApplication {
        val client = createSessionClient()
        val id = client.withProject(client.withOrganization())

        client.client.post(Projects.ById.Tokens(projectId = id)) {
            setBodyForm(
                Tokens.PERMISSION to Permission.READ,
                Tokens.DESCRIPTION to "Read"
            )
        }.bodyAsText()

        var tokens = client.client
            .get(Projects.ById.Tokens(projectId = id))
            .bodyAsJson<List<SelectByProject>>()
        client.client.delete(Tokens.ById(id = tokens[0].id))

        tokens = client.client.get(Projects.ById.Tokens(projectId = id)).bodyAsJson()
        assert(tokens.isEmpty())
    }

    @Test
    fun unauthenticatedAccess() = testApplication {
        val client = createClient {
            install(Resources)
            followRedirects = false
        }
        val id = createSessionClient().run {
            withProject(withOrganization())
        }

        var response = client.get(Projects.ById(id = id))
        assert(response.status == HttpStatusCode.Unauthorized)

        response = client.put(Projects.ById(id = id)) {
            setBodyForm(Projects.NAME to "Test")
        }
        assert(response.status == HttpStatusCode.Unauthorized)

        response = client.delete(Projects.ById(id = id))
        assert(response.status == HttpStatusCode.Unauthorized)

        response = client.post(Projects.ById.Tokens(projectId = id)) {
            setBodyForm(Tokens.PERMISSION to Permission.EVAL, Tokens.DESCRIPTION to "Eval")
        }
        assert(response.status == HttpStatusCode.Unauthorized)
    }

    @Test
    fun apiLatestOptional() = testApplication {
        val client = createSessionClient()
        val versions = listOf(PATH_LATEST, "")

        val createResponses = versions.map {
            val id = client.withOrganization()
            client.client.post(
                "$it${client.client.href(Organizations.ById.Projects(organizationId = id))}"
            ) {
                setBodyForm(Projects.NAME to "Test")
            }
        }
        assert(createResponses[0].status == createResponses[1].status)

        val ids = createResponses.map {
            it.headers[HttpHeaders.Location]!!.substringAfterLast('/').toLong()
        }
        val updateResponses = versions.zip(ids).map { (version, id) ->
            client.client.put("$version${client.client.href(Projects.ById(id = id))}") {
                setBodyForm(Projects.NAME to "Test updated")
            }
        }
        assert(updateResponses[0].status == updateResponses[1].status)

        val deleteResponse = versions.zip(ids).map { (version, id) ->
            client.client.delete("$version${client.client.href(Projects.ById(id = id))}")
        }
        assert(deleteResponse[0].status == deleteResponse[1].status)
    }
}
