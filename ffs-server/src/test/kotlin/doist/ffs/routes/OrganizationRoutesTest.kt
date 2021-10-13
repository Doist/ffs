package doist.ffs.routes

import doist.ffs.OrganizationSerializer
import doist.ffs.module
import io.ktor.server.testing.withTestApplication
import kotlin.test.Test

class OrganizationRoutesTest {
    @Test
    fun testOrganizationLifecycle() {
        withTestApplication({
            configureDatabaseForLifecycleTest()
            module()
        }) {
            val initialName = "old-test-organization"
            val updatedName = "new-test-organization"

            // Ensure no organizations exist.
            assertResourceCount(PATH_ORGANIZATIONS, OrganizationSerializer, 0)

            // Create an organization.
            val location = assertResourceCreates(PATH_ORGANIZATIONS, listOf("name" to initialName))

            // Ensure organization is now listed.
            assertResourceCount(PATH_ORGANIZATIONS, OrganizationSerializer, 1)

            // Fetch it via the location response header.
            var organization = assertResourceAtPath(location, OrganizationSerializer)
            assert(organization.name == initialName)

            // Update its name.
            assertResourceUpdates(location, listOf("name" to updatedName))

            // Fetch it again via the location response header.
            organization = assertResourceAtPath(location, OrganizationSerializer)
            assert(organization.name == updatedName)

            // Delete it.
            assertResourceDeletes(location)

            // Ensure no organizations exist.
            assertResourceCount(PATH_ORGANIZATIONS, OrganizationSerializer, 0)
        }
    }
}
