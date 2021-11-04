package doist.ffs.routes

import doist.ffs.db.Project
import doist.ffs.db.capturingLastInsertId
import doist.ffs.db.organizations
import doist.ffs.db.projects
import doist.ffs.module
import doist.ffs.plugins.database
import io.ktor.server.application.Application
import io.ktor.server.testing.withTestApplication
import kotlin.test.Test

class ProjectRoutesTest {
    @Test
    fun testProjectCreate() = withTestApplication(Application::module) {
        val organizationId = createOrganization(application)
        assertResourceCreates(
            PATH_PROJECTS,
            listOf("organization_id" to organizationId.toString(), "name" to NAME)
        )
        val organizations =
            application.database.projects.selectByOrganization(organizationId).executeAsList()
        assert(organizations.size == 1)
        assert(organizations[0].name == NAME)
    }

    @Test
    fun testProjectCreateLocation() = withTestApplication(Application::module) {
        val organizationId = createOrganization(application)
        val location = assertResourceCreates(
            PATH_PROJECTS,
            listOf("organization_id" to organizationId.toString(), "name" to NAME)
        )
        assertResource<Project>(location)
    }

    @Test
    fun testProjectRead() = withTestApplication(Application::module) {
        val organizationId = createOrganization(application)
        val pathProjectsForOrganization = "$PATH_PROJECTS?organization_id=$organizationId"
        assertResourceCount<Project>(pathProjectsForOrganization, 0)
        val id = application.database.capturingLastInsertId {
            projects.insert(organization_id = organizationId, name = NAME)
        }
        assertResourceCount<Project>(pathProjectsForOrganization, 1)
        assertResource<Project>(PATH_PROJECT(id)) { project ->
            assert(project.id == id)
            assert(project.organization_id == organizationId)
            assert(project.name == NAME)
            application.database.projects.delete(id)
            assertResourceCount<Project>(pathProjectsForOrganization, 0)
        }
    }

    @Test
    fun testProjectUpdate() = withTestApplication(Application::module) {
        val organizationId = createOrganization(application)
        val id = application.database.capturingLastInsertId {
            projects.insert(organization_id = organizationId, name = NAME)
        }
        assertResourceUpdates(PATH_PROJECT(id), listOf("name" to NAME_UPDATED))
        val project = application.database.projects.select(id).executeAsOne()
        assert(project.id == id)
        assert(project.organization_id == organizationId)
        assert(project.name == NAME_UPDATED)
    }

    @Test
    fun testProjectDelete() = withTestApplication(Application::module) {
        val organizationId = createOrganization(application)
        val id = application.database.capturingLastInsertId {
            projects.insert(organization_id = organizationId, name = NAME)
        }
        assertResourceDeletes(PATH_PROJECT(id))
        val project = application.database.projects.select(id).executeAsOneOrNull()
        assert(project == null)
    }

    private fun createOrganization(application: Application): Long =
        application.database.capturingLastInsertId {
            organizations.insert(name = "test-organization")
        }

    companion object {
        private const val NAME = "old-test-project"
        private const val NAME_UPDATED = "new-test-project"
    }
}
