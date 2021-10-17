package doist.ffs.routes

import doist.ffs.ProjectSerializer
import doist.ffs.ext.capturingLastInsertId
import doist.ffs.ext.database
import doist.ffs.ext.organizations
import doist.ffs.ext.projects
import kotlin.test.Test

class ProjectRoutesTest {
    @Test
    fun testProjectCreate() = withTestApplication {
        val organizationId = createOrganization()
        assertResourceCreates(
            PATH_PROJECTS,
            listOf("organization_id" to organizationId.toString(), "name" to NAME)
        )
        val organizations = database.projects.selectByOrganization(organizationId).executeAsList()
        assert(organizations.size == 1)
        assert(organizations[0].name == NAME)
    }

    @Test
    fun testProjectCreateLocation() = withTestApplication {
        val organizationId = createOrganization()
        val location = assertResourceCreates(
            PATH_PROJECTS,
            listOf("organization_id" to organizationId.toString(), "name" to NAME)
        )
        assertResource(location, ProjectSerializer)
    }

    @Test
    fun testProjectRead() = withTestApplication {
        val organizationId = createOrganization()
        val pathProjectsForOrganization = "$PATH_PROJECTS?organization_id=$organizationId"
        assertResourceCount(pathProjectsForOrganization, ProjectSerializer, 0)
        val id = database.capturingLastInsertId {
            projects.insert(organization_id = organizationId, name = NAME)
        }
        assertResourceCount(pathProjectsForOrganization, ProjectSerializer, 1)
        assertResource(PATH_PROJECT(id), ProjectSerializer) { project ->
            assert(project.id == id)
            assert(project.organization_id == organizationId)
            assert(project.name == NAME)
            database.projects.delete(id)
            assertResourceCount(pathProjectsForOrganization, ProjectSerializer, 0)
        }
    }

    @Test
    fun testProjectUpdate() = withTestApplication {
        val organizationId = createOrganization()
        val id = database.capturingLastInsertId {
            projects.insert(organization_id = organizationId, name = NAME)
        }
        assertResourceUpdates(PATH_PROJECT(id), listOf("name" to NAME_UPDATED))
        val project = database.projects.select(id).executeAsOne()
        assert(project.id == id)
        assert(project.organization_id == organizationId)
        assert(project.name == NAME_UPDATED)
    }

    @Test
    fun testProjectDelete() = withTestApplication {
        val organizationId = createOrganization()
        val id = database.capturingLastInsertId {
            projects.insert(organization_id = organizationId, name = NAME)
        }
        assertResourceDeletes(PATH_PROJECT(id))
        val project = database.projects.select(id).executeAsOneOrNull()
        assert(project == null)
    }

    private fun createOrganization(): Long = database.capturingLastInsertId {
        organizations.insert(name = "test-organization")
    }

    companion object {
        private const val NAME = "old-test-project"
        private const val NAME_UPDATED = "new-test-project"
    }
}
