package doist.ffs.routes

import doist.ffs.OrganizationSerializer
import doist.ffs.capturingLastInsertId
import doist.ffs.organizations
import doist.ffs.withDatabase
import kotlin.test.Test

class OrganizationRoutesTest {
    @Test
    fun testOrganizationCreate() = withTestApplication {
        assertResourceCreates(PATH_ORGANIZATIONS, listOf("name" to NAME))
        withDatabase { db ->
            val organizations = db.organizations.selectAll().executeAsList()
            assert(organizations.size == 1)
            assert(organizations[0].name == NAME)
        }
    }

    @Test
    fun testOrganizationCreateLocation() = withTestApplication {
        val location = assertResourceCreates(PATH_ORGANIZATIONS, listOf("name" to NAME))
        assertResource(location, OrganizationSerializer)
    }

    @Test
    fun testOrganizationRead() = withTestApplication {
        assertResourceCount(PATH_ORGANIZATIONS, OrganizationSerializer, 0)
        val id = withDatabase { db ->
            db.capturingLastInsertId {
                organizations.insert(name = NAME)
            }
        }
        assertResourceCount(PATH_ORGANIZATIONS, OrganizationSerializer, 1)
        assertResource(PATH_ORGANIZATION(id), OrganizationSerializer) { organization ->
            assert(organization.id == id)
            assert(organization.name == NAME)
        }
        withDatabase { db ->
            db.organizations.delete(id)
        }
        assertResourceCount(PATH_ORGANIZATIONS, OrganizationSerializer, 0)
    }

    @Test
    fun testOrganizationUpdate() = withTestApplication {
        val id = withDatabase { db ->
            db.capturingLastInsertId {
                organizations.insert(name = NAME)
            }
        }
        assertResourceUpdates(PATH_ORGANIZATION(id), listOf("name" to NAME_UPDATED))
        withDatabase { db ->
            val organization = db.organizations.select(id).executeAsOne()
            assert(organization.name == NAME_UPDATED)
        }
    }

    @Test
    fun testOrganizationDelete() = withTestApplication {
        val id = withDatabase { db ->
            db.capturingLastInsertId {
                organizations.insert(name = NAME)
            }
        }
        assertResourceDeletes(PATH_ORGANIZATION(id))
        withDatabase { db ->
            val organization = db.organizations.select(id).executeAsOneOrNull()
            assert(organization == null)
        }
    }

    companion object {
        private const val NAME = "old-test-organization"
        private const val NAME_UPDATED = "new-test-organization"
    }
}
