package doist.ffs.routes

import doist.ffs.ProjectSerializer
import doist.ffs.capturingLastInsertId
import doist.ffs.module
import doist.ffs.organizations
import doist.ffs.withDatabase
import io.ktor.server.testing.withTestApplication
import kotlin.test.Test

class ProjectRoutesTest {
    @Test
    fun testProjectLifecycle() {
        withTestApplication({
            configureDatabaseForLifecycleTest()
            module()
        }) {
            val initialName = "old-test-project"
            val updatedName = "new-test-project"

            // Setup an organization to own projects in testing.
            val organizationId = withDatabase { db ->
                db.capturingLastInsertId { db.organizations.insert("test-organization") }
            }

            val pathProjectsForOrganization = "$PATH_PROJECTS?organization_id=$organizationId"

            // Ensure no projects exist.
            assertResourceCount(pathProjectsForOrganization, ProjectSerializer, 0)

            // Create a project.
            val location = assertResourceCreates(
                PATH_PROJECTS,
                listOf("organization_id" to organizationId.toString(), "name" to initialName)
            )

            // Ensure project is now listed.
            assertResourceCount(pathProjectsForOrganization, ProjectSerializer, 1)

            // Fetch it via the location response header.
            var organization = assertResourceAtPath(location, ProjectSerializer)
            assert(organization.name == initialName)

            // Update its name.
            assertResourceUpdates(location, listOf("name" to updatedName))

            // Fetch it again via the location response header.
            organization = assertResourceAtPath(location, ProjectSerializer)
            assert(organization.name == updatedName)

            // Delete it.
            assertResourceDeletes(location)

            // Ensure no projects exist.
            assertResourceCount(pathProjectsForOrganization, ProjectSerializer, 0)

            // Clenaup test organization.
            withDatabase { db -> db.organizations.delete(organizationId) }
        }
    }
}
