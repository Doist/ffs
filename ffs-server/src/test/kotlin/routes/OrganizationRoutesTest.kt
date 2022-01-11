package doist.ffs.routes

import doist.ffs.db.Organization
import doist.ffs.db.Project
import doist.ffs.db.RoleEnum
import doist.ffs.db.SelectOrganizationByUser
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
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OrganizationRoutesTest {
    @Test
    fun create() = testApplication {
        val client = createUserClient()
        val createResponse = client.client.post(PATH_ORGANIZATIONS) {
            setBodyForm("name" to "Test")
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
            .get(PATH_ORGANIZATIONS)
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
            client.client.get(PATH_ORGANIZATION(42))
        }
        assertFailsWith<ClientRequestException> {
            client.client.delete(PATH_ORGANIZATION(42))
        }
    }

    @Test
    fun update() = testApplication {
        val client = createUserClient()
        val id = client.withOrganization()
        var organization = client.client.get(PATH_ORGANIZATION(id)).bodyAsJson<Organization>()

        val name = "${organization.name} updated"
        client.client.put(PATH_ORGANIZATION(id)) {
            setBodyForm("name" to name)
        }

        organization = client.client.get(PATH_ORGANIZATION(id)).bodyAsJson()
        assert(organization.name == name)
    }

    @Test
    fun userManagement() = testApplication {
        val client = createUserClient()
        val roles = RoleEnum.values().toList()
        assert(roles[0] == RoleEnum.ADMIN)
        val ids = List(roles.size) { client.withOrganization(RoleEnum.ADMIN) }

        for (i in 1 until roles.size) {
            client.client.put("${PATH_ORGANIZATION(ids[i - 1])}$PATH_USERS") {
                setBodyForm("user_id" to client.userId, "role" to roles[i])
            }
        }
        client.client.delete("${PATH_ORGANIZATION(ids[roles.size - 1])}$PATH_USERS") {
            setBodyForm("user_id" to client.userId)
        }

        val organizations = client.client
            .get(PATH_ORGANIZATIONS)
            .bodyAsJson<List<SelectOrganizationByUser>>()

        assert(organizations.size == ids.size - 1)
        assert(roles.drop(1).toSet() == organizations.map { it.role }.toSet())
        assert(ids.dropLast(1).toSet() == organizations.map { it.id }.toSet())
    }

    @Test
    fun updateUserMissingUserId() = testApplication {
        val client = createUserClient()
        val id = client.withOrganization()
        assertFailsWith<ClientRequestException> {
            client.client.put("${PATH_ORGANIZATION(id)}$PATH_USERS") {
                setBodyForm("role" to RoleEnum.USER)
            }
        }
    }

    @Test
    fun updateUserMissingName() = testApplication {
        val client = createUserClient()
        val id = client.withOrganization()
        assertFailsWith<ClientRequestException> {
            client.client.put("${PATH_ORGANIZATION(id)}$PATH_USERS")
        }
    }

    @Test
    fun deleteUserMissingUserId() = testApplication {
        val client = createUserClient()
        val id = client.withOrganization()
        assertFailsWith<ClientRequestException> {
            client.client.delete("${PATH_ORGANIZATION(id)}$PATH_USERS")
        }
    }

    @Test
    fun projectManagement() = testApplication {
        val client = createUserClient()
        val organizationId = client.withOrganization()
        val projectIds = List(2) { client.withProject(organizationId) }

        val projects = client.client
            .get("${PATH_ORGANIZATION(organizationId)}$PATH_PROJECTS")
            .bodyAsJson<List<Project>>()

        assertEquals(projectIds.toSet(), projects.map { it.id }.toSet())
    }

    @Test
    fun delete() = testApplication {
        val client = createUserClient()
        val id = client.withOrganization()

        client.client.delete(PATH_ORGANIZATION(id)) {
            setBodyForm("user_id" to client.userId)
        }

        assertFailsWith<ClientRequestException> {
            client.client.get(PATH_ORGANIZATION(id)).bodyAsJson<Organization?>()
        }
    }

    @Test
    fun unauthenticatedAccess() = testApplication {
        val client = createClient {
            install(HttpCookies)
            followRedirects = false
        }
        assertFailsWith<RedirectResponseException> {
            client.post(PATH_ORGANIZATIONS) {
                setBodyForm("name" to "Test")
            }
        }
        assertFailsWith<RedirectResponseException> {
            client.get(PATH_ORGANIZATIONS)
        }
        val id = createUserClient().withOrganization()
        assertFailsWith<RedirectResponseException> {
            client.get(PATH_ORGANIZATION(id))
        }
        assertFailsWith<RedirectResponseException> {
            client.put(PATH_ORGANIZATION(id)) {
                setBodyForm("name" to "Test")
            }
        }
        assertFailsWith<RedirectResponseException> {
            client.delete(PATH_ORGANIZATION(id))
        }
    }
}
