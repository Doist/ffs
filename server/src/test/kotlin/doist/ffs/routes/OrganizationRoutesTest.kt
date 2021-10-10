package doist.ffs.routes

import doist.ffs.OrganizationSerializer
import doist.ffs.db.Organization
import doist.ffs.module
import io.ktor.config.MapApplicationConfig
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.formUrlEncode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.contentType
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.test.Test

class OrganizationRoutesTest {
    @Test
    fun testOrganizationLifecycle() {
        withTestApplication({
            (environment.config as MapApplicationConfig).apply {
                val path = kotlin.io.path.createTempFile().toString()
                put("database.path", path)
            }
            module()
        }) {
            val initialName = "old-test-organization"
            val updatedName = "new-test-organization"

            // Ensure no organizations exist.
            assertOrganizationsSize(0)

            // Create an organization.
            val location = assertOrganizationCreated(initialName)

            // Ensure organization is now listed.
            assertOrganizationsSize(1)

            // Fetch it via the location response header.
            var organization = assertOrganizationInLocation(location)
            assert(organization.name == initialName)

            // Update its name.
            assertOrganizationUpdates(location, updatedName)

            // Fetch it again via the location response header.
            organization = assertOrganizationInLocation(location)
            assert(organization.name == updatedName)

            // Delete it.
            assertOrganizationDeletes(location)

            // Ensure no organizations exist.
            assertOrganizationsSize(0)
        }
    }

    private fun TestApplicationEngine.assertOrganizationsSize(size: Int) {
        with(handleRequest(HttpMethod.Get, "/organizations")) {
            assert(response.status() == HttpStatusCode.OK)
            assert(response.contentType().match(ContentType.Application.Json))
            val organizations = Json.decodeFromString(
                ListSerializer(OrganizationSerializer),
                response.content!!
            )
            assert(organizations.size == size)
        }
    }

    private fun TestApplicationEngine.assertOrganizationCreated(name: String): String {
        return with(
            handleRequest(HttpMethod.Post, "/organizations") {
                addHeader(
                    HttpHeaders.ContentType,
                    ContentType.Application.FormUrlEncoded.toString()
                )
                setBody(listOf("name" to name).formUrlEncode())
            }
        ) {
            assert(response.status() == HttpStatusCode.Created)
            response.headers[HttpHeaders.Location].also {
                assert(it != null)
            }
        }!!
    }

    private fun TestApplicationEngine.assertOrganizationInLocation(location: String): Organization {
        return with(handleRequest(HttpMethod.Get, location)) {
            assert(response.status() == HttpStatusCode.OK)
            assert(response.contentType().match(ContentType.Application.Json))
            Json.decodeFromString(OrganizationSerializer, response.content!!)
        }
    }

    private fun TestApplicationEngine.assertOrganizationUpdates(location: String, name: String) {
        with(
            handleRequest(HttpMethod.Put, location) {
                addHeader(
                    HttpHeaders.ContentType,
                    ContentType.Application.FormUrlEncoded.toString()
                )
                setBody(listOf("name" to name).formUrlEncode())
            }
        ) {
            assert(response.status() == HttpStatusCode.NoContent)
        }
    }

    private fun TestApplicationEngine.assertOrganizationDeletes(location: String) {
        with(handleRequest(HttpMethod.Delete, location)) {
            assert(response.status() == HttpStatusCode.NoContent)
        }
    }
}
