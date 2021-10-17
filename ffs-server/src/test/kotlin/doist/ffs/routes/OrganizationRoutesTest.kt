package doist.ffs.routes

import doist.ffs.OrganizationSerializer
import doist.ffs.ext.capturingLastInsertId
import doist.ffs.ext.database
import doist.ffs.ext.organizations
import kotlin.test.Test

class OrganizationRoutesTest {
    @Test
    fun testOrganizationCreate() = withTestApplication {
        assertResourceCreates(PATH_ORGANIZATIONS, listOf("name" to NAME))
        val organizations = database.organizations.selectAll().executeAsList()
        assert(organizations.size == 1)
        assert(organizations[0].name == NAME)
    }

    @Test
    fun testOrganizationCreateLocation() = withTestApplication {
        val location = assertResourceCreates(PATH_ORGANIZATIONS, listOf("name" to NAME))
        assertResource(location, OrganizationSerializer)
    }

    @Test
    fun testOrganizationRead() = withTestApplication {
        assertResourceCount(PATH_ORGANIZATIONS, OrganizationSerializer, 0)
        val id = database.capturingLastInsertId {
            organizations.insert(name = NAME)
        }
        assertResourceCount(PATH_ORGANIZATIONS, OrganizationSerializer, 1)
        assertResource(PATH_ORGANIZATION(id), OrganizationSerializer) { organization ->
            assert(organization.id == id)
            assert(organization.name == NAME)
        }
        database.organizations.delete(id)
        assertResourceCount(PATH_ORGANIZATIONS, OrganizationSerializer, 0)
    }

    @Test
    fun testOrganizationUpdate() = withTestApplication {
        val id = database.capturingLastInsertId {
            organizations.insert(name = NAME)
        }
        assertResourceUpdates(PATH_ORGANIZATION(id), listOf("name" to NAME_UPDATED))
        val organization = database.organizations.select(id).executeAsOne()
        assert(organization.name == NAME_UPDATED)
    }

    @Test
    fun testOrganizationDelete() = withTestApplication {
        val id = database.capturingLastInsertId {
            organizations.insert(name = NAME)
        }
        assertResourceDeletes(PATH_ORGANIZATION(id))
        val organization = database.organizations.select(id).executeAsOneOrNull()
        assert(organization == null)
    }

    companion object {
        private const val NAME = "old-test-organization"
        private const val NAME_UPDATED = "new-test-organization"
    }
}
