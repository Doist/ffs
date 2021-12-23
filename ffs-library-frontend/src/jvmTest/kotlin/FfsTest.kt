package doist.ffs

import doist.ffs.env.ENV_DEVICE_LOCALE
import doist.ffs.env.ENV_DEVICE_NAME
import doist.ffs.env.ENV_INTERNAL_ROLLOUT_ID
import doist.ffs.env.ENV_USER_EMAIL
import doist.ffs.env.ENV_USER_ID
import doist.ffs.uuid.UUID
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FfsTest {
    private val engine = MockEngine {
        // Respond with error by default so that SSE terminates.
        respondError(HttpStatusCode.InternalServerError)
    }

    @Test
    fun testApiToken() = runBlocking {
        Ffs(API_TOKEN, PROJECT_ID).initialize(engine).join()
        val request = engine.requestHistory.last()
        assertEquals("Bearer $API_TOKEN", request.headers[HttpHeaders.Authorization])
    }

    @Test
    fun testParams() = runBlocking {
        Ffs(API_TOKEN, PROJECT_ID).initialize(engine).join()
        val request = engine.requestHistory.last()
        assertContains(request.url.parameters.names(), "project_id")
        assertContains(request.url.parameters.names(), "env")
    }

    @Test
    fun testEnv() = runBlocking {
        Ffs(API_TOKEN, PROJECT_ID).apply {
            setRolloutId(ROLLOUT_ID)
            setUserId(USER_ID)
            setUserEmail(USER_EMAIL)
            setDeviceName(DEVICE_NAME)
            setDeviceLocale(DEVICE_LOCALE)
            putNumber(KEY_RANDOM_NUMBER, RANDOM_NUMBER)
            putBoolean(KEY_RANDOM_BOOLEAN, RANDOM_BOOLEAN)
            putListString(KEY_RANDOM_LIST, RANDOM_LIST)
        }.initialize(engine).join()
        val request = engine.requestHistory.last()
        val env = Json.decodeFromString<JsonObject>(request.url.parameters["env"]!!)
        assertEquals(env[ENV_INTERNAL_ROLLOUT_ID], JsonPrimitive(ROLLOUT_ID))
        assertEquals(env[ENV_USER_ID], JsonPrimitive(USER_ID))
        assertEquals(env[ENV_USER_EMAIL], JsonPrimitive(USER_EMAIL))
        assertEquals(env[ENV_DEVICE_NAME], JsonPrimitive(DEVICE_NAME))
        assertEquals(env[ENV_DEVICE_LOCALE], JsonPrimitive(DEVICE_LOCALE))
        assertEquals(env[KEY_RANDOM_NUMBER], JsonPrimitive(RANDOM_NUMBER))
        assertEquals(env[KEY_RANDOM_BOOLEAN], JsonPrimitive(RANDOM_BOOLEAN))
        assertEquals(env[KEY_RANDOM_LIST], JsonArray(RANDOM_LIST.map { JsonPrimitive(it) }))
    }

    @Test
    fun testLiveUpdatesFlag() = runBlocking {
        Ffs(API_TOKEN, PROJECT_ID).initialize(engine).join()
        var request = engine.requestHistory.last()
        assertTrue(
            request.headers[HttpHeaders.Accept]!!.contains(ContentType.Text.EventStream.toString())
        )

        Ffs(API_TOKEN, PROJECT_ID, liveUpdates = true).initialize(engine).join()
        request = engine.requestHistory.last()
        assertTrue(
            request.headers[HttpHeaders.Accept]!!.contains(ContentType.Text.EventStream.toString())
        )

        Ffs(API_TOKEN, PROJECT_ID, liveUpdates = false).initialize(engine).join()
        request = engine.requestHistory.last()
        assertFalse(
            request.headers[HttpHeaders.Accept]!!.contains(ContentType.Text.EventStream.toString())
        )
    }

    @Test
    fun testFlagEnabled() = runBlocking {
        val ffs = Ffs(API_TOKEN, PROJECT_ID, liveUpdates = false)
        assertFalse(ffs.isEnabled("test"))
        ffs.initialize(
            MockEngine {
                respond(
                    content = ByteReadChannel("""{"test": true}"""),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        ).join()
        assertTrue(ffs.isEnabled("test"))
    }

    companion object {
        val API_TOKEN = UUID.generateV4()
        val PROJECT_ID = Random.nextLong()

        val ROLLOUT_ID = UUID.generateV4()

        val USER_ID = UUID.generateV4()
        val USER_EMAIL = UUID.generateV4()

        val DEVICE_NAME = UUID.generateV4()
        val DEVICE_LOCALE = UUID.generateV4()

        val RANDOM_NUMBER = Random.nextInt()
        val RANDOM_BOOLEAN = Random.nextBoolean()
        val RANDOM_LIST = listOf("a", "b", "c")

        const val KEY_RANDOM_NUMBER = "random_number"
        const val KEY_RANDOM_BOOLEAN = "random_boolean"
        const val KEY_RANDOM_LIST = "random_list"
    }
}
