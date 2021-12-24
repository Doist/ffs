package doist.ffs

import doist.ffs.env.ENV_DEVICE_LOCALE
import doist.ffs.env.ENV_DEVICE_NAME
import doist.ffs.env.ENV_INTERNAL_ROLLOUT_ID
import doist.ffs.env.ENV_USER_EMAIL
import doist.ffs.env.ENV_USER_ID
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// This can and should be multiplatform, but building fails due to a coroutines version mismatch.
// See: https://youtrack.jetbrains.com/issue/KT-50222
class ClientTest {
    class MockClient(liveUpdates: Boolean = true) : Client<Unit>(
        TOKEN, 1L, "https://doist.com", "/dummy", liveUpdates, Unit.serializer()
    ) {
        override fun isEnabled(name: String): Boolean = false
    }

    private val engine = MockEngine {
        // Respond with error by default so that SSE terminates.
        respondError(HttpStatusCode.InternalServerError)
    }

    @Test
    fun testApiToken() = runTest {
        MockClient().initialize(engine).join()
        val request = engine.requestHistory.last()
        assertEquals("Bearer $TOKEN", request.headers[HttpHeaders.Authorization])
    }

    @Test
    fun testParams() = runTest {
        MockClient().initialize(engine).join()
        val request = engine.requestHistory.last()
        assertContains(request.url.parameters.names(), "project_id")
        assertContains(request.url.parameters.names(), "env")
    }

    @Test
    fun testEnv() = runTest {
        MockClient().apply {
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
    fun testLiveUpdatesFlag() = runTest {
        MockClient().initialize(engine).join()
        var request = engine.requestHistory.last()
        assertTrue(
            request.headers[HttpHeaders.Accept]!!.contains(ContentType.Text.EventStream.toString())
        )

        MockClient(liveUpdates = true).initialize(engine).join()
        request = engine.requestHistory.last()
        assertTrue(
            request.headers[HttpHeaders.Accept]!!.contains(ContentType.Text.EventStream.toString())
        )

        MockClient(liveUpdates = false).initialize(engine).join()
        request = engine.requestHistory.last()
        assertFalse(
            request.headers[HttpHeaders.Accept]!!.contains(ContentType.Text.EventStream.toString())
        )
    }

    companion object {
        const val TOKEN = "123456789abcdef"

        const val ROLLOUT_ID = "a-random-string"

        const val USER_ID = "1"
        const val USER_EMAIL = "goncalo@doist.com"

        const val DEVICE_NAME = "Pixel 6 Pro"
        const val DEVICE_LOCALE = "en-US"

        const val RANDOM_NUMBER = 42
        const val RANDOM_BOOLEAN = true
        val RANDOM_LIST = listOf("a", "b", "c")

        const val KEY_RANDOM_NUMBER = "random_number"
        const val KEY_RANDOM_BOOLEAN = "random_boolean"
        const val KEY_RANDOM_LIST = "random_list"
    }
}
