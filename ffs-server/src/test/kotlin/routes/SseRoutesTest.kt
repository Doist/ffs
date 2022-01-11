package routes

import doist.ffs.auth.Permission
import doist.ffs.db.Flag
import doist.ffs.db.TokenGenerator
import doist.ffs.db.capturingLastInsertId
import doist.ffs.db.flags
import doist.ffs.db.organizations
import doist.ffs.db.projects
import doist.ffs.db.tokens
import doist.ffs.env.ENV_INTERNAL_ROLLOUT_ID
import doist.ffs.module
import doist.ffs.plugins.database
import doist.ffs.routes.PATH_EVAL
import doist.ffs.routes.PATH_FLAGS
import doist.ffs.serialization.json
import doist.ffs.sse.SSE_FIELD_PREFIX_DATA
import doist.ffs.sse.SSE_FIELD_PREFIX_ID
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.encodeURLPath
import io.ktor.http.isSuccess
import io.ktor.server.application.Application
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.withTestApplication
import io.ktor.util.pipeline.execute
import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.Test

/**
 * Tests for server-sent events (SSE) based endpoints.
 *
 * It uses the deprecated `withTestApplication` API (and direct database access) to circumvent a
 * deadlock when using the new approach, occurring in multiple variations of the following skeleton:
 *
 * ```
 * @Test
 * fun testStreamGet() = testApplication {
 *     val client = createTokenClient(Permission.READ)
 *     val projectId = client.projectId
 *     var eventCount = 0
 *
 *     client.userClient.client.post("${PATH_PROJECT(projectId)}$PATH_FLAGS") {
 *         setBodyForm("name" to "test", "rule" to "true")
 *     }
 *
 *     client.client.launch {
 *         client.client.stream(PATH_FLAGS) {
 *             eventCount++
 *             val flags = json.decodeFromString<List<Flag>>(it.data)
 *             assert(flags.size == 1)
 *             assert(flags[0].name == "test")
 *             assert(flags[0].rule == "true")
 *         }
 *     }
 *
 *     client.userClient.client.post("${PATH_PROJECT(projectId)}$PATH_FLAGS") {
 *         setBodyForm("name" to "test", "rule" to "true")
 *     }
 *
 *     assert(eventCount == 1)
 * }
 * ```
 *
 * Circumventing this would probably require that the server and client NOT share the same
 * dispatcher (Dispatchers.IO), but setting a different one for either is not currently possible.
 */
@Suppress("LongMethod")
class SseRoutesTest {
    @Test
    fun testFlagStream(): Unit = withTestApplication(Application::module) {
        val organizationId = application.database.capturingLastInsertId {
            organizations.insert(name = "test-organization")
        }
        val projectId = application.database.capturingLastInsertId {
            projects.insert(organization_id = organizationId, name = "test-project")
        }
        val token = TokenGenerator.generate(Permission.READ)
        application.database.tokens.insert(
            token = token,
            project_id = projectId,
            description = "test-read-token"
        )
        handleSse(
            uri = PATH_FLAGS,
            setup = {
                addHeader(HttpHeaders.Authorization, "Bearer $token")
            }
        ) { channel ->
            val readLine = suspend { channel.readUTF8Line()!! }

            // Channel starts out empty as there is no data.
            assert(channel.availableForRead == 0)

            // Create a flag and check it is sent.
            var flag = application.database.run {
                flags.run {
                    val id = capturingLastInsertId {
                        insert(project_id = projectId, name = "true", rule = "true")
                    }
                    select(id).executeAsOne()
                }
            }
            readLine().startsWith(SSE_FIELD_PREFIX_ID)
            readLine().let { line ->
                line.startsWith(SSE_FIELD_PREFIX_DATA)
                val flags = json.decodeFromString<List<Flag>>(
                    line.substring(SSE_FIELD_PREFIX_DATA.length)
                )
                assert(flags.size == 1)
                assert(flags[0].name == "true")
                assert(flags[0].rule == "true")
                assert(flags[0].archived_at == null)
            }
            readLine().isEmpty()

            // Update the flag and check it is sent again.
            flag = application.database.flags.run {
                update(id = flag.id, name = "false", rule = "false")
                select(flag.id).executeAsOne()
            }
            readLine().startsWith(SSE_FIELD_PREFIX_ID)
            readLine().let { line ->
                line.startsWith(SSE_FIELD_PREFIX_DATA)
                val flags = json.decodeFromString<List<Flag>>(
                    line.substring(SSE_FIELD_PREFIX_DATA.length)
                )
                assert(flags.size == 1)
                assert(flags[0].name == "false")
                assert(flags[0].rule == "false")
                assert(flags[0].archived_at == null)
            }
            readLine().isEmpty()

            // Archive the flag and check it is sent again.
            flag = application.database.flags.run {
                archive(id = flag.id)
                select(flag.id).executeAsOne()
            }
            readLine().startsWith(SSE_FIELD_PREFIX_ID)
            readLine().let { line ->
                line.startsWith(SSE_FIELD_PREFIX_DATA)
                val flags = json.decodeFromString<List<Flag>>(
                    line.substring(SSE_FIELD_PREFIX_DATA.length)
                )
                assert(flags.size == 1)
                assert(flags[0].name == "false")
                assert(flags[0].rule == "false")
                assert(flags[0].archived_at != null)
            }
            readLine().isEmpty()

            // Without further changes, channel is empty.
            assert(channel.availableForRead == 0)
        }
    }

    @Test
    fun testFlagEvalStream(): Unit = withTestApplication(Application::module) {
        val organizationId = application.database.capturingLastInsertId {
            organizations.insert(name = "test-organization")
        }
        val projectId = application.database.capturingLastInsertId {
            projects.insert(organization_id = organizationId, name = "test-project")
        }
        val token = TokenGenerator.generate(Permission.EVAL)
        application.database.tokens.insert(
            token = token,
            project_id = projectId,
            description = "test-eval-token"
        )
        val env = buildJsonObject {
            put(ENV_INTERNAL_ROLLOUT_ID, "123456789abcdef")
            put("number", 3)
        }
        handleSse(
            uri = "$PATH_FLAGS$PATH_EVAL?env=${json.encodeToString(env).encodeURLPath()}",
            setup = {
                addHeader(HttpHeaders.Authorization, "Bearer $token")
            }
        ) { channel ->
            val readLine = suspend { channel.readUTF8Line()!! }

            // Channel starts out empty as there is no data.
            assert(channel.availableForRead == 0)

            // Create a flag and check its eval is sent.
            val flag = application.database.run {
                flags.run {
                    val id = capturingLastInsertId {
                        insert(
                            project_id = projectId,
                            name = "true",
                            rule = "gt(env[\"number\"], 2)"
                        )
                    }
                    select(id).executeAsOne()
                }
            }
            readLine().startsWith(SSE_FIELD_PREFIX_ID)
            readLine().let { line ->
                line.startsWith(SSE_FIELD_PREFIX_DATA)
                val flagEvals = json.decodeFromString<Map<String, Boolean>>(
                    line.substring(SSE_FIELD_PREFIX_DATA.length)
                )
                assert(flagEvals == mapOf("true" to true))
            }
            readLine().isEmpty()

            // Update the flag rule and check its eval is sent again.
            application.database.flags.run {
                update(id = flag.id, name = "false", rule = "lt(env[\"number\"], 2)")
                select(flag.id).executeAsOne()
            }
            readLine().startsWith(SSE_FIELD_PREFIX_ID)
            readLine().let { line ->
                line.startsWith(SSE_FIELD_PREFIX_DATA)
                val flagEvals = json.decodeFromString<Map<String, Boolean>>(
                    line.substring(SSE_FIELD_PREFIX_DATA.length)
                )
                assert(flagEvals == mapOf("false" to false))
            }
            readLine().isEmpty()

            // Update the flag name and ensure the channel remains empty.
            application.database.flags.run {
                update(id = flag.id, name = "maybe", rule = "0.5")
                select(flag.id).executeAsOne()
            }
            assert(channel.availableForRead == 0)
        }
    }
}

/**
 * Make a test request that sets up an SSE session and invokes a [callback] function to
 * receive events from the server.
 */
@Suppress("EXPERIMENTAL_API_USAGE_FUTURE_ERROR")
private fun TestApplicationEngine.handleSse(
    uri: String,
    setup: TestApplicationRequest.() -> Unit = {},
    callback: suspend TestApplicationCall.(incoming: ByteReadChannel) -> Unit
): TestApplicationCall {
    val call = createCall(closeRequest = false) {
        this.uri = uri
        addHeader(HttpHeaders.Accept, ContentType.Text.EventStream.toString())
        setup()
        bodyChannel = ByteChannel(true)
    }

    launch(call.coroutineContext) {
        // Execute server side.
        pipeline.execute(call)
    }

    runBlocking(call.coroutineContext) {
        // responseChannelDeferred is internal, so we wait like this.
        // Ref: https://github.com/ktorio/ktor/blob/c5877a22c91fd693ea6dcd0b4e1924f05d3b6825/ktor-server/ktor-server-test-host/jvm/src/io/ktor/server/testing/TestApplicationEngine.kt#L225-L230
        var responseChannel: ByteReadChannel?
        do {
            // Ensure status is absent or valid.
            val status = call.response.status()
            if (status?.isSuccess() == false) {
                throw IllegalStateException(status.toString())
            }

            // Suspend, then try to grab response channel.
            yield()
            // websocketChannel is just responseChannel internally.
            responseChannel = call.response.websocketChannel()
        } while (responseChannel == null)

        // Execute client side.
        call.callback(responseChannel)
    }

    return call
}
