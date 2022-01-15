package doist.ffs.routes

import doist.ffs.db.Flag
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
import org.junit.jupiter.api.Test
import routes.PATH_LATEST
import kotlin.test.assertFailsWith

class FlagRoutesTest {
    @Test
    fun create() = testApplication {
        val client = createUserClient()
        val projectId = client.withProject(client.withOrganization())
        val createResponse = client.client.post("${PATH_PROJECT(projectId)}$PATH_FLAGS") {
            setBodyForm("name" to "test", "rule" to "true")
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
        val client = createUserClient()
        val projectId = client.withProject(client.withOrganization())

        assertFailsWith<ClientRequestException> {
            client.client.post("${PATH_PROJECT(projectId)}$PATH_FLAGS") {
                setBodyForm("name" to "test", "rule" to "(")
            }
        }
    }

    @Test
    fun get() = testApplication {
        val client = createUserClient()
        val projectId = client.withProject(client.withOrganization())
        val ids = List(5) { client.withFlag(projectId) }

        val flags = client.client
            .get("${PATH_PROJECT(projectId)}$PATH_FLAGS")
            .bodyAsJson<List<Flag>>()
        assert(ids.size == flags.size)
        assert(ids.toSet() == flags.map { it.id }.toSet())
    }

    @Test
    fun update() = testApplication {
        val client = createUserClient()
        val id = client.withFlag(client.withProject(client.withOrganization()))

        var flag = client.client.get(PATH_FLAG(id)).bodyAsJson<Flag>()
        val name = "${flag.name} updated"
        val rule = "0.667"
        client.client.put(PATH_FLAG(id)) {
            setBodyForm("name" to name, "rule" to rule)
        }

        flag = client.client.get(PATH_FLAG(id)).bodyAsJson()
        assert(flag.name == name)
        assert(flag.rule == rule)
    }

    @Test
    fun updateInvalidRule() = testApplication {
        val client = createUserClient()
        val id = client.withFlag(client.withProject(client.withOrganization()))

        assertFailsWith<ClientRequestException> {
            client.client.put(PATH_FLAG(id)) {
                setBodyForm("rule" to "]")
            }
        }
    }

    @Test
    fun archive() = testApplication {
        val client = createUserClient()
        val id = client.withFlag(client.withProject(client.withOrganization()))

        var flag = client.client.get(PATH_FLAG(id)).bodyAsJson<Flag>()
        assert(flag.archived_at == null)

        client.client.put("${PATH_FLAG(id)}/$PATH_ARCHIVE")
        flag = client.client.get(PATH_FLAG(id)).bodyAsJson()
        assert(flag.archived_at != null)

        client.client.delete("${PATH_FLAG(id)}/$PATH_ARCHIVE")
        flag = client.client.get(PATH_FLAG(id)).bodyAsJson()
        assert(flag.archived_at == null)
    }

    @Test
    fun unauthenticatedAccess() = testApplication {
        val client = createClient {
            install(HttpCookies)
            followRedirects = false
        }
        val id = createUserClient().run {
            withFlag(withProject(withOrganization()))
        }
        assertFailsWith<RedirectResponseException> {
            client.get(PATH_FLAG(id))
        }
        assertFailsWith<RedirectResponseException> {
            client.put(PATH_FLAG(id)) {
                setBodyForm("name" to "test")
            }
        }
        assertFailsWith<RedirectResponseException> {
            client.put("${PATH_FLAG(id)}/$PATH_ARCHIVE")
        }
    }

    @Test
    fun apiLatestOptional() = testApplication {
        val client = createUserClient()
        val versions = listOf(PATH_LATEST, "")

        val createResponses = versions.map {
            client.client.post(
                "$it${PATH_PROJECT(client.withProject(client.withOrganization()))}$PATH_FLAGS"
            ) {
                setBodyForm("name" to "test", "rule" to "true")
            }
        }
        assert(createResponses[0].status == createResponses[1].status)

        val ids = createResponses.map {
            it.headers[HttpHeaders.Location]!!.substringAfterLast('/')
        }
        val updateResponses = versions.zip(ids).map { (version, id) ->
            client.client.put("$version${PATH_FLAG(id)}") {
                setBodyForm("name" to "test updated", "rule" to "false")
            }
        }
        assert(updateResponses[0].status == updateResponses[1].status)

        val archiveResponses = versions.zip(ids).map { (version, id) ->
            client.client.put("$version${PATH_FLAG(id)}/$PATH_ARCHIVE")
        }
        assert(archiveResponses[0].status == archiveResponses[1].status)
    }
}
