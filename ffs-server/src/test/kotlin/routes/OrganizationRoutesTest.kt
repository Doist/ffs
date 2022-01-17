package doist.ffs.routes

import doist.ffs.db.Organization
import doist.ffs.db.Project
import doist.ffs.db.RoleEnum
import doist.ffs.db.SelectOrganizationByUser
import doist.ffs.endpoints.Organizations
import doist.ffs.endpoints.Organizations.Companion.ById
import doist.ffs.ext.bodyAsJson
import doist.ffs.ext.setBodyForm
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.cookies.HttpCookies
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
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import routes.PATH_LATEST
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OrganizationRoutesTest {
    @Test
    fun create() = testApplication {
        val client = createUserClient()
        val createResponse = client.client.post(Organizations()) {
            setBodyForm(Organizations.NAME to "Test")
        }
        assert(createResponse.status == HttpStatusCode.Created)
        val resource = createResponse.headers[HttpHeaders.Location]
        assert(resource != null)

        val organization = client.client.get(resource!!).bodyAsJson<Organization>()
        assert(organization.name == "Test")
    }

    @Test
    fun get() = testApplication {
        val client = createUserClient()
        val roles = RoleEnum.values().toList()
        val ids = roles.map { client.withOrganization(it) }

        val organizations = client.client
            .get(Organizations())
            .bodyAsJson<List<SelectOrganizationByUser>>()
        assert(ids.size == organizations.size)
        assert(ids.toSet() == organizations.map { it.id }.toSet())
        assert(roles.toSet() == organizations.map { it.role }.toSet())
    }

    @Test
    fun getNonexistentId() = testApplication {
        val client = createUserClient()

        // Nonexistent id.
        assertFailsWith<ClientRequestException> {
            client.client.get(Organizations.ById(id = 42))
        }
        assertFailsWith<ClientRequestException> {
            client.client.delete(Organizations.ById(id = 42))
        }
    }

    @Test
    fun update() = testApplication {
        val client = createUserClient()
        val id = client.withOrganization()
        var organization = client.client.get(Organizations.ById(id = id)).bodyAsJson<Organization>()

        val name = "${organization.name} updated"
        client.client.put(Organizations.ById(id = id)) {
            setBodyForm(Organizations.NAME to name)
        }

        organization = client.client.get(Organizations.ById(id = id)).bodyAsJson()
        assert(organization.name == name)
    }

    @Test
    fun userManagement() = testApplication {
        val client = createUserClient()
        val roles = RoleEnum.values().toList()
        assert(roles[0] == RoleEnum.ADMIN)
        val ids = List(roles.size) { client.withOrganization(RoleEnum.ADMIN) }

        for (i in 1 until roles.size) {
            client.client.put(
                Organizations.ById.Users.ById(id = ids[i - 1], user_id = client.userId)
            ) {
                setBodyForm(Organizations.USER_ID to client.userId, Organizations.ROLE to roles[i])
            }
        }
        client.client.delete(
            Organizations.ById.Users.ById(id = ids[roles.size - 1], user_id = client.userId)
        ) {
            setBodyForm(Organizations.USER_ID to client.userId)
        }

        val organizations = client.client
            .get(Organizations())
            .bodyAsJson<List<SelectOrganizationByUser>>()

        assert(organizations.size == ids.size - 1)
        assert(roles.drop(1).toSet() == organizations.map { it.role }.toSet())
        assert(ids.dropLast(1).toSet() == organizations.map { it.id }.toSet())
    }

    @Test
    fun updateUserMissingName() = testApplication {
        val client = createUserClient()
        val id = client.withOrganization()
        assertFailsWith<ClientRequestException> {
            client.client.put(Organizations.ById.Users.ById(id = id, user_id = client.userId))
        }
    }

    @Test
    fun projectManagement() = testApplication {
        val client = createUserClient()
        val organizationId = client.withOrganization()
        val projectIds = List(2) { client.withProject(organizationId) }

        val projects = client.client
            .get("/organizations/$organizationId$PATH_PROJECTS")
            .bodyAsJson<List<Project>>()

        assertEquals(projectIds.toSet(), projects.map { it.id }.toSet())
    }

    @Test
    fun delete() = testApplication {
        val client = createUserClient()
        val id = client.withOrganization()

        client.client.delete(Organizations.ById.Users.ById(id = id, user_id = client.userId))

        assertFailsWith<ClientRequestException> {
            client.client.get(Organizations.ById(id = id)).bodyAsJson<Organization?>()
        }
    }

    @Test
    fun unauthenticatedAccess() = testApplication {
        val client = createClient {
            install(Resources)
            install(HttpCookies)
            followRedirects = false
        }
        assertFailsWith<RedirectResponseException> {
            client.post(Organizations()) {
                setBodyForm(Organizations.NAME to "Test")
            }
        }
        assertFailsWith<RedirectResponseException> {
            client.get(Organizations())
        }
        val id = createUserClient().withOrganization()
        assertFailsWith<RedirectResponseException> {
            client.get(Organizations.ById(id = id))
        }
        assertFailsWith<RedirectResponseException> {
            client.put(Organizations.ById(id = id)) {
                setBodyForm(Organizations.NAME to "Test")
            }
        }
        assertFailsWith<RedirectResponseException> {
            client.delete(Organizations.ById(id = id))
        }
    }

    @Test
    fun apiLatestOptional() = testApplication {
        val client = createUserClient()
        val versions = listOf(PATH_LATEST, "")

        val createResponses = versions.map {
            client.client.post("$it${client.client.href(Organizations())}") {
                setBodyForm(Organizations.NAME to "Test")
            }
        }
        assert(createResponses[0].status == createResponses[1].status)

        val ids = createResponses.map {
            it.headers[HttpHeaders.Location]!!.substringAfterLast('/').toLong()
        }
        val updateResponses = versions.zip(ids).map { (version, id) ->
            client.client.put("$version${client.client.href(Organizations.ById(id = id))}") {
                setBodyForm(Organizations.NAME to "Test updated")
            }
        }
        assert(updateResponses[0].status == updateResponses[1].status)

        val deleteResponse = versions.zip(ids).map { (version, id) ->
            client.client.delete("$version${client.client.href(Organizations.ById(id = id))}")
        }
        assert(deleteResponse[0].status == deleteResponse[1].status)
    }
}
