package doist.ffs.routes

import doist.ffs.db.Flag
import doist.ffs.endpoints.Flags
import doist.ffs.endpoints.Flags.Companion.Archive
import doist.ffs.endpoints.Projects
import doist.ffs.endpoints.Projects.Companion.Flags
import doist.ffs.ext.bodyAsJson
import doist.ffs.ext.setBodyForm
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.plugins.resources.delete
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.href
import io.ktor.client.plugins.resources.post
import io.ktor.client.plugins.resources.put
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import org.junit.Test
import routes.PATH_LATEST

class FlagRoutesTest {
    @Test
    fun create() = testApplication {
        val client = createSessionClient()
        val projectId = client.withProject(client.withOrganization())
        val createResponse = client.client.post(Projects.ById.Flags(projectId = projectId)) {
            setBodyForm(
                Flags.NAME to "test",
                Flags.RULE to "true"
            )
        }
        assert(createResponse.status == HttpStatusCode.Created)

        val resource = createResponse.headers[HttpHeaders.Location]
        assert(resource != null)

        val flag = client.client.get(resource!!).bodyAsJson<Flag>()
        assert(flag.name == "test")
        assert(flag.rule == "true")
    }

    @Test
    fun createInvalidRule() = testApplication {
        val client = createSessionClient()
        val projectId = client.withProject(client.withOrganization())

        val response = client.client.post(Projects.ById.Flags(projectId = projectId)) {
            setBodyForm(
                Flags.NAME to "test",
                Flags.RULE to "("
            )
        }
        assert(response.status == HttpStatusCode.BadRequest)
    }

    @Test
    fun get() = testApplication {
        val client = createSessionClient()
        val projectId = client.withProject(client.withOrganization())
        val ids = List(5) { client.withFlag(projectId) }

        val flags = client.client
            .get(Projects.ById.Flags(projectId = projectId))
            .bodyAsJson<List<Flag>>()
        assert(ids.size == flags.size)
        assert(ids.toSet() == flags.map { it.id }.toSet())
    }

    @Test
    fun update() = testApplication {
        val client = createSessionClient()
        val id = client.withFlag(client.withProject(client.withOrganization()))

        var flag = client.client.get(Flags.ById(id = id)).bodyAsJson<Flag>()
        val name = "${flag.name} updated"
        val rule = "0.667"
        client.client.put(Flags.ById(id = id)) {
            setBodyForm(
                Flags.NAME to name,
                Flags.RULE to rule
            )
        }

        flag = client.client.get(Flags.ById(id = id)).bodyAsJson()
        assert(flag.name == name)
        assert(flag.rule == rule)
    }

    @Test
    fun updateInvalidRule() = testApplication {
        val client = createSessionClient()
        val id = client.withFlag(client.withProject(client.withOrganization()))

        val response = client.client.put(Flags.ById(id = id)) {
            setBodyForm(Flags.RULE to "]")
        }
        assert(response.status == HttpStatusCode.BadRequest)
    }

    @Test
    fun archive() = testApplication {
        val client = createSessionClient()
        val id = client.withFlag(client.withProject(client.withOrganization()))

        var flag = client.client.get(Flags.ById(id = id)).bodyAsJson<Flag>()
        assert(flag.archived_at == null)

        client.client.put(Flags.ById.Archive(id = id))
        flag = client.client.get(Flags.ById(id = id)).bodyAsJson()
        assert(flag.archived_at != null)

        client.client.delete(Flags.ById.Archive(id = id))
        flag = client.client.get(Flags.ById(id = id)).bodyAsJson()
        assert(flag.archived_at == null)
    }

    @Test
    fun unauthenticatedAccess() = testApplication {
        val client = createClient {
            install(Resources)
            followRedirects = false
        }
        val id = createSessionClient().run {
            withFlag(withProject(withOrganization()))
        }

        var response = client.get(Flags.ById(id = id))
        assert(response.status == HttpStatusCode.Unauthorized)

        response = client.put(Flags.ById(id = id)) {
            setBodyForm(Flags.NAME to "test")
        }
        assert(response.status == HttpStatusCode.Unauthorized)

        response = client.put(Flags.ById.Archive(id = id))
        assert(response.status == HttpStatusCode.Unauthorized)
    }

    @Test
    fun apiLatestOptional() = testApplication {
        val client = createSessionClient()
        val versions = listOf(PATH_LATEST, "")

        val createResponses = versions.map {
            val projectId = client.withProject(client.withOrganization())
            client.client.post(
                "$it${client.client.href(Projects.ById.Flags(projectId = projectId))}"
            ) {
                setBodyForm(
                    Flags.NAME to "test",
                    Flags.RULE to "true"
                )
            }
        }
        assert(createResponses[0].status == createResponses[1].status)

        val ids = createResponses.map {
            it.headers[HttpHeaders.Location]!!.substringAfterLast('/').toLong()
        }
        val updateResponses = versions.zip(ids).map { (version, id) ->
            client.client.put("$version${client.client.href(Flags.ById(id = id))}") {
                setBodyForm(Flags.NAME to "test updated", Flags.RULE to "false")
            }
        }
        assert(updateResponses[0].status == updateResponses[1].status)

        val archiveResponses = versions.zip(ids).map { (version, id) ->
            client.client.put("$version${client.client.href(Flags.ById.Archive(id = id))}")
        }
        assert(archiveResponses[0].status == archiveResponses[1].status)
    }
}
