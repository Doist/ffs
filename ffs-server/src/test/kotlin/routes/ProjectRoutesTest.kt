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
        val organizationId = setupOrganization(application)
        assertResourceCreates(
            uri = PATH_PROJECTS,
            args = listOf("organization_id" to organizationId.toString(), "name" to NAME)
        )
        val organizations =
            application.database.projects.selectByOrganization(organizationId).executeAsList()
        assert(organizations.size == 1)
        assert(organizations[0].name == NAME)
    }

    @Test
    fun testProjectCreateLocation() = withTestApplication(Application::module) {
        val organizationId = setupOrganization(application)
        val location = assertResourceCreates(
            uri = PATH_PROJECTS,
            args = listOf("organization_id" to organizationId.toString(), "name" to NAME)
        )
        assertResource<Project>(uri = location)
    }

    @Test
    fun testProjectCount() = withTestApplication(Application::module) {
        val organizationId = setupOrganization(application)
        val path = "$PATH_PROJECTS?organization_id=$organizationId"
        assertResourceCount<Project>(uri = path, count = 0)
        val id = application.database.capturingLastInsertId {
            projects.insert(organization_id = organizationId, name = NAME)
        }
        assertResourceCount<Project>(uri = path, count = 1)
        application.database.projects.delete(id)
        assertResourceCount<Project>(path, count = 0)
    }

    @Test
    fun testProjectRead() = withTestApplication(Application::module) {
        val organizationId = setupOrganization(application)
        val id = application.database.capturingLastInsertId {
            projects.insert(organization_id = organizationId, name = NAME)
        }
        assertResource<Project>(uri = PATH_PROJECT(id)) { project ->
            assert(project.id == id)
            assert(project.organization_id == organizationId)
            assert(project.name == NAME)
        }
    }

    @Test
    fun testProjectUpdate() = withTestApplication(Application::module) {
        val organizationId = setupOrganization(application)
        val id = application.database.capturingLastInsertId {
            projects.insert(organization_id = organizationId, name = NAME)
        }
        assertResourceUpdates(uri = PATH_PROJECT(id), args = listOf("name" to NAME_UPDATED))
        val project = application.database.projects.select(id).executeAsOne()
        assert(project.id == id)
        assert(project.organization_id == organizationId)
        assert(project.name == NAME_UPDATED)
    }

    @Test
    fun testProjectDelete() = withTestApplication(Application::module) {
        val organizationId = setupOrganization(application)
        val id = application.database.capturingLastInsertId {
            projects.insert(organization_id = organizationId, name = NAME)
        }
        assertResourceDeletes(uri = PATH_PROJECT(id))
        val project = application.database.projects.select(id).executeAsOneOrNull()
        assert(project == null)
    }

    companion object {
        private const val NAME = "test-project"
        private const val NAME_UPDATED = "new-test-project"

        private fun setupOrganization(application: Application): Long {
            return application.database.capturingLastInsertId {
                organizations.insert(name = "test-organization")
            }
        }
    }
}
