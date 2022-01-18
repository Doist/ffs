package doist.ffs

import doist.ffs.endpoints.Organizations
import doist.ffs.endpoints.Organizations.Companion.Projects
import doist.ffs.endpoints.Projects
import doist.ffs.endpoints.Users
import doist.ffs.models.Organization
import doist.ffs.models.Project
import doist.ffs.models.User
import doist.ffs.plugins.SessionHeader
import doist.ffs.plugins.SessionStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ContentNegotiation
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.DEFAULT_PORT
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.browser.localStorage
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.w3c.dom.get
import org.w3c.dom.set
import react.ChildrenBuilder
import react.StateSetter

val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

private val scope = MainScope()

private val client = HttpClient {
    install(Resources)
    install(SessionHeader) {
        name = HttpHeaders.Authorization
        storage = object : SessionStorage {
            override fun get(url: Url) = localStorage[KEY_SESSION]
            override fun set(url: Url, value: String?) = if (value != null) {
                localStorage[KEY_SESSION] = value
            } else {
                localStorage.removeItem(KEY_SESSION)
            }
        }
    }
    install(ContentNegotiation) {
        json(json = json)
    }
    install(DefaultRequest) {
        host = js("SERVER_HOST") as String? ?: "api.ffs.delivery/v1"
        port = js("SERVER_PORT") as Int? ?: DEFAULT_PORT
    }
    install(HttpRequestRetry) {
        retryOnServerErrors(maxRetries = 3)
        exponentialDelay()
    }
}

fun <T> ChildrenBuilder.api(
    setSession: StateSetter<String?>,
    method: suspend HttpClient.() -> T
) = scope.launch {
    val session = localStorage[KEY_SESSION]
    runCatching {
        client.method()
    }.onFailure {
        console.warn(it)
    }
    val newSession = localStorage[KEY_SESSION]
    if (session != newSession) {
        setSession(newSession)
    }
}

suspend fun HttpClient.register(name: String, email: String, password: String): User =
    post(Users.Register()) {
        setBodyParameters(
            Users.NAME to name,
            Users.EMAIL to email,
            Users.PASSWORD to password
        )
    }.body()

suspend fun HttpClient.login(email: String, password: String): User =
    post(Users.Login()) {
        setBodyParameters(
            Users.EMAIL to email,
            Users.PASSWORD to password
        )
    }.body()

suspend fun HttpClient.logout() {
    post(Users.Logout())
}

suspend fun HttpClient.listOrganizations(): List<Organization> =
    get(Organizations()).body()

suspend fun HttpClient.createOrganization(name: String): Long =
    post(Organizations()) {
        setBodyParameters(Organizations.NAME to name)
    }.getIdFromLocation()

suspend fun HttpClient.listProjects(organizationId: Long): List<Project> =
    get(Organizations.ById.Projects(organizationId)).body()

suspend fun HttpClient.createProject(name: String, organizationId: Long): Long =
    post(Organizations.ById.Projects(organizationId)) {
        setBodyParameters(Projects.NAME to name)
    }.getIdFromLocation()

private fun HttpRequestBuilder.setBodyParameters(vararg args: Pair<String, Any>) {
    setBody(
        FormDataContent(
            Parameters.build {
                for ((name, value) in args) {
                    append(name, value.toString())
                }
            }
        )
    )
}

private fun HttpResponse.getIdFromLocation() =
    headers[HttpHeaders.Location]!!.substringAfterLast('/').toLong()
