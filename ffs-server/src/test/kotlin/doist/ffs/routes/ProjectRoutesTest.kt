package doist.ffs.routes

import doist.ffs.ProjectSerializer
import doist.ffs.capturingLastInsertId
import doist.ffs.organizations
import doist.ffs.projects
import doist.ffs.withDatabase
import kotlin.test.Test

class ProjectRoutesTest {
    @Test
    fun testProjectCreate() = withTestApplication {
        val organizationId = createOrganization()
        assertResourceCreates(
            PATH_PROJECTS,
            listOf("organization_id" to organizationId.toString(), "name" to NAME)
        )
        withDatabase { db ->
            val organizations = db.projects.selectByOrganization(organizationId).executeAsList()
            assert(organizations.size == 1)
            assert(organizations[0].name == NAME)
        }
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
        val id = withDatabase { db ->
            db.capturingLastInsertId {
                projects.insert(organization_id = organizationId, name = NAME)
            }
        }
        assertResourceCount(pathProjectsForOrganization, ProjectSerializer, 1)
        assertResource(PATH_PROJECT(id), ProjectSerializer) { project ->
            assert(project.id == id)
            assert(project.organization_id == organizationId)
            assert(project.name == NAME)
        }
        withDatabase { db ->
            db.projects.delete(id)
        }
        assertResourceCount(pathProjectsForOrganization, ProjectSerializer, 0)
    }

    @Test
    fun testProjectUpdate() = withTestApplication {
        val organizationId = createOrganization()
        val id = withDatabase { db ->
            db.capturingLastInsertId {
                projects.insert(organization_id = organizationId, name = NAME)
            }
        }
        assertResourceUpdates(PATH_PROJECT(id), listOf("name" to NAME_UPDATED))
        withDatabase { db ->
            val project = db.projects.select(id).executeAsOne()
            assert(project.id == id)
            assert(project.organization_id == organizationId)
            assert(project.name == NAME_UPDATED)
        }
    }

    @Test
    fun testProjectDelete() = withTestApplication {
        val organizationId = createOrganization()
        val id = withDatabase { db ->
            db.capturingLastInsertId {
                projects.insert(organization_id = organizationId, name = NAME)
            }
        }
        assertResourceDeletes(PATH_PROJECT(id))
        withDatabase { db ->
            val project = db.projects.select(id).executeAsOneOrNull()
            assert(project == null)
        }
    }

    private fun createOrganization(): Long = withDatabase { db ->
        db.capturingLastInsertId { db.organizations.insert(name = "test-organization") }
    }

    companion object {
        private const val NAME = "old-test-project"
        private const val NAME_UPDATED = "new-test-project"
    }
}
