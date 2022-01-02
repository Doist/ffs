package doist.ffs.routes

import doist.ffs.db.Organization
import doist.ffs.db.capturingLastInsertId
import doist.ffs.db.organizations
import doist.ffs.module
import doist.ffs.plugins.database
import io.ktor.server.application.Application
import io.ktor.server.testing.withTestApplication
import kotlin.test.Test

class OrganizationRoutesTest {
    @Test
    fun testOrganizationCreate() = withTestApplication(Application::module) {
        assertResourceCreates(uri = PATH_ORGANIZATIONS, args = listOf("name" to NAME))
        val organizations = application.database.organizations.selectAll().executeAsList()
        assert(organizations.size == 1)
        assert(organizations[0].name == NAME)
    }

    @Test
    fun testOrganizationCreateLocation() = withTestApplication(Application::module) {
        val location = assertResourceCreates(
            uri = PATH_ORGANIZATIONS,
            args = listOf("name" to NAME)
        )
        assertResource<Organization>(uri = location)
    }

    @Test
    fun testOrganizationCount() = withTestApplication(Application::module) {
        assertResourceCount<Organization>(uri = PATH_ORGANIZATIONS, count = 0)
        val id = application.database.capturingLastInsertId {
            organizations.insert(name = NAME)
        }
        assertResourceCount<Organization>(uri = PATH_ORGANIZATIONS, count = 1)
        application.database.organizations.delete(id)
        assertResourceCount<Organization>(uri = PATH_ORGANIZATIONS, count = 0)
    }

    @Test
    fun testOrganizationRead() = withTestApplication(Application::module) {
        val id = application.database.capturingLastInsertId {
            organizations.insert(name = NAME)
        }
        assertResource<Organization>(uri = PATH_ORGANIZATION(id)) { organization ->
            assert(organization.id == id)
            assert(organization.name == NAME)
        }
    }

    @Test
    fun testOrganizationUpdate() = withTestApplication(Application::module) {
        val id = application.database.capturingLastInsertId {
            organizations.insert(name = NAME)
        }
        assertResourceUpdates(uri = PATH_ORGANIZATION(id), args = listOf("name" to NAME_UPDATED))
        val organization = application.database.organizations.select(id).executeAsOne()
        assert(organization.name == NAME_UPDATED)
    }

    @Test
    fun testOrganizationDelete() = withTestApplication(Application::module) {
        val id = application.database.capturingLastInsertId {
            organizations.insert(name = NAME)
        }
        assertResourceDeletes(uri = PATH_ORGANIZATION(id))
        val organization = application.database.organizations.select(id).executeAsOneOrNull()
        assert(organization == null)
    }

    companion object {
        private const val NAME = "test-organization"
        private const val NAME_UPDATED = "new-test-organization"
    }
}
