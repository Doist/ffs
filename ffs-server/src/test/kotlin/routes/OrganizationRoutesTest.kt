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
        assertResourceCreates(PATH_ORGANIZATIONS, listOf("name" to NAME))
        val organizations = application.database.organizations.selectAll().executeAsList()
        assert(organizations.size == 1)
        assert(organizations[0].name == NAME)
    }

    @Test
    fun testOrganizationCreateLocation() = withTestApplication(Application::module) {
        val location = assertResourceCreates(PATH_ORGANIZATIONS, listOf("name" to NAME))
        assertResource<Organization>(location)
    }

    @Test
    fun testOrganizationRead() = withTestApplication(Application::module) {
        assertResourceCount<Organization>(PATH_ORGANIZATIONS, 0)
        val id = application.database.capturingLastInsertId {
            organizations.insert(name = NAME)
        }
        assertResourceCount<Organization>(PATH_ORGANIZATIONS, 1)
        assertResource<Organization>(PATH_ORGANIZATION(id)) { organization ->
            assert(organization.id == id)
            assert(organization.name == NAME)
        }
        application.database.organizations.delete(id)
        assertResourceCount<Organization>(PATH_ORGANIZATIONS, 0)
    }

    @Test
    fun testOrganizationUpdate() = withTestApplication(Application::module) {
        val id = application.database.capturingLastInsertId {
            organizations.insert(name = NAME)
        }
        assertResourceUpdates(PATH_ORGANIZATION(id), listOf("name" to NAME_UPDATED))
        val organization = application.database.organizations.select(id).executeAsOne()
        assert(organization.name == NAME_UPDATED)
    }

    @Test
    fun testOrganizationDelete() = withTestApplication(Application::module) {
        val id = application.database.capturingLastInsertId {
            organizations.insert(name = NAME)
        }
        assertResourceDeletes(PATH_ORGANIZATION(id))
        val organization = application.database.organizations.select(id).executeAsOneOrNull()
        assert(organization == null)
    }

    companion object {
        private const val NAME = "old-test-organization"
        private const val NAME_UPDATED = "new-test-organization"
    }
}
