package doist.ffs.routes

import doist.ffs.FlagSerializer
import doist.ffs.capturingLastInsertId
import doist.ffs.module
import doist.ffs.organizations
import doist.ffs.projects
import doist.ffs.withDatabase
import io.ktor.server.testing.withTestApplication
import kotlin.test.Test

class FlagRoutesTest {
    @Test
    fun testFlagLifecycle() {
        withTestApplication({
            configureDatabaseForLifecycleTest()
            module()
        }) {
            val initialName = "old-test-flag"
            val updatedName = "new-test-flag"
            val initialRule = "1"
            val updatedRule = "0"

            // Setup a project to own projects in testing.
            val organizationId = withDatabase { db ->
                db.capturingLastInsertId { db.organizations.insert("test-organization") }
            }
            val projectId = withDatabase { db ->
                db.capturingLastInsertId {
                    db.projects.insert(organization_id = organizationId, name = "test-project")
                }
            }

            val pathFlagsForProject = "$PATH_FLAGS?project_id=$projectId"

            // Ensure no flags exist.
            assertResourceCount(pathFlagsForProject, FlagSerializer, 0)

            // Create a flag.
            val location = assertResourceCreates(
                PATH_FLAGS,
                listOf(
                    "project_id" to projectId.toString(),
                    "name" to initialName,
                    "rule" to initialRule
                )
            )

            // Ensure flag is now listed.
            assertResourceCount(pathFlagsForProject, FlagSerializer, 1)

            // Fetch it via the location response header.
            var organization = assertResourceAtPath(location, FlagSerializer)
            assert(organization.name == initialName)

            // Update its name.
            assertResourceUpdates(location, listOf("name" to updatedName))

            // Fetch it again via the location response header.
            organization = assertResourceAtPath(location, FlagSerializer)
            assert(organization.name == updatedName)

            // Update its rule.
            assertResourceUpdates(location, listOf("rule" to updatedRule))

            // Fetch it again via the location response header.
            organization = assertResourceAtPath(location, FlagSerializer)
            assert(organization.rule == updatedRule)

            // Delete it.
            assertResourceDeletes(location)

            // Ensure no flags exist.
            assertResourceCount(pathFlagsForProject, FlagSerializer, 0)

            // Clenaup test organization.
            withDatabase { db -> db.organizations.delete(organizationId) }
        }
    }
}
