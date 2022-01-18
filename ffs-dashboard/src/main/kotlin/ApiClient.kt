package doist.ffs

import doist.ffs.endpoints.Organizations
import doist.ffs.endpoints.Users
import doist.ffs.models.Organization
import doist.ffs.models.User
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ContentNegotiation
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.setBody
import io.ktor.http.DEFAULT_PORT
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import react.ChildrenBuilder
import react.StateSetter

val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

private val scope = MainScope()

private val client = HttpClient {
    install(Resources)
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

fun <T> ChildrenBuilder.api(setUser: StateSetter<User?>, method: suspend HttpClient.() -> T): Job {
    return scope.launch {
        runCatching {
            client.method()
        }.onFailure { error ->
            when (error) {
                is ClientRequestException -> {
                    if (error.response.status == HttpStatusCode.Unauthorized) {
                        setUser(null)
                    }
                }
            }
        }
    }
}

suspend fun HttpClient.register(name: String, email: String, password: String): User =
    post(Users.Register()) {
        setBody(
            Users.NAME to name,
            Users.EMAIL to email,
            Users.PASSWORD to password
        )
    }.body()

suspend fun HttpClient.login(email: String, password: String): User = post(Users.Login()) {
    setBody(
        Users.EMAIL to email,
        Users.PASSWORD to password
    )
}.body()

suspend fun HttpClient.logout() {
    post(Users.Logout())
}

private fun HttpRequestBuilder.setBody(vararg args: Pair<String, Any>) {
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
