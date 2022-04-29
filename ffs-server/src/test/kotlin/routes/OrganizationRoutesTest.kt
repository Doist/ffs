package doist.ffs.routes

import doist.ffs.db.Organization
import doist.ffs.db.Project
import doist.ffs.db.Role
import doist.ffs.db.SelectOrganizationByUserId
import doist.ffs.endpoints.Organizations
import doist.ffs.endpoints.Organizations.Companion.ById
import doist.ffs.endpoints.Organizations.Companion.Projects
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
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import routes.PATH_LATEST
import kotlin.test.Test
import kotlin.test.assertEquals

class OrganizationRoutesTest {
    @Test
    fun create() = testApplication {
        val client = createSessionClient()
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
        val client = createSessionClient()
        val roles = Role.values().toList()
        val ids = roles.map { client.withOrganization(it) }

        val organizations = client.client
            .get(Organizations())
            .bodyAsJson<List<SelectOrganizationByUserId>>()
        assert(ids.size == organizations.size)
        assert(ids.toSet() == organizations.map { it.id }.toSet())
        assert(roles.toSet() == organizations.map { it.role }.toSet())
    }

    @Test
    fun getNonexistentId() = testApplication {
        val client = createSessionClient()

        // Nonexistent id.
        var response = client.client.get(Organizations.ById(id = 42))
        assert(response.status == HttpStatusCode.Forbidden)

        response = client.client.delete(Organizations.ById(id = 42))
        assert(response.status == HttpStatusCode.Forbidden)
    }

    @Test
    fun update() = testApplication {
        val client = createSessionClient()
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
        val client = createSessionClient()
        val roles = Role.values().toList()
        assert(roles[0] == Role.ADMIN)
        val ids = List(roles.size) { client.withOrganization(Role.ADMIN) }

        for (i in 1 until roles.size) {
            client.client.put(
                Organizations.ById.Members.ById(id = ids[i - 1], userId = client.userId)
            ) {
                setBodyForm(
                    Organizations.ById.Members.USER_ID to client.userId,
                    Organizations.ById.Members.ROLE to roles[i]
                )
            }
        }
        client.client.delete(
            Organizations.ById.Members.ById(id = ids[roles.size - 1], userId = client.userId)
        ) {
            setBodyForm(Organizations.ById.Members.USER_ID to client.userId)
        }

        val organizations = client.client
            .get(Organizations())
            .bodyAsJson<List<SelectOrganizationByUserId>>()

        assert(organizations.size == ids.size - 1)
        assert(roles.drop(1).toSet() == organizations.map { it.role }.toSet())
        assert(ids.dropLast(1).toSet() == organizations.map { it.id }.toSet())
    }

    @Test
    fun updateUserMissingRole() = testApplication {
        val client = createSessionClient()
        val id = client.withOrganization()

        val response = client.client.put(
            Organizations.ById.Members.ById(id = id, userId = client.userId)
        ) {
            setBodyForm()
        }
        assert(response.status == HttpStatusCode.BadRequest)
    }

    @Test
    fun projectManagement() = testApplication {
        val client = createSessionClient()
        val organizationId = client.withOrganization()
        val projectIds = List(2) { client.withProject(organizationId) }

        val projects = client.client
            .get(Organizations.ById.Projects(organizationId = organizationId))
            .bodyAsJson<List<Project>>()

        assertEquals(projectIds.toSet(), projects.map { it.id }.toSet())
    }

    @Test
    fun delete() = testApplication {
        val client = createSessionClient()
        val id = client.withOrganization()

        client.client.delete(Organizations.ById.Members.ById(id = id, userId = client.userId))

        val response = client.client.get(Organizations.ById(id = id))
        assert(response.status == HttpStatusCode.Forbidden)
    }

    @Test
    fun unauthenticatedAccess() = testApplication {
        val client = createClient {
            install(Resources)
            followRedirects = false
        }

        var response = client.post(Organizations()) {
            setBodyForm(Organizations.NAME to "Test")
        }
        assert(response.status == HttpStatusCode.Unauthorized)

        response = client.get(Organizations())
        assert(response.status == HttpStatusCode.Unauthorized)

        val id = createSessionClient().withOrganization()
        response = client.get(Organizations.ById(id = id))
        assert(response.status == HttpStatusCode.Unauthorized)

        response = client.put(Organizations.ById(id = id)) {
            setBodyForm(Organizations.NAME to "Test")
        }
        assert(response.status == HttpStatusCode.Unauthorized)

        response = client.delete(Organizations.ById(id = id))
        assert(response.status == HttpStatusCode.Unauthorized)
    }

    @Test
    fun apiLatestOptional() = testApplication {
        val client = createSessionClient()
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
