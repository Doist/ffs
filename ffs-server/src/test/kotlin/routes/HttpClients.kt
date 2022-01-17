@file:Suppress("MatchingDeclarationName")

package doist.ffs.routes

import doist.ffs.auth.Permission
import doist.ffs.db.RoleEnum
import doist.ffs.endpoints.Organizations
import doist.ffs.endpoints.Organizations.Companion.ById
import doist.ffs.endpoints.Users
import doist.ffs.ext.setBodyForm
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.plugins.resources.post
import io.ktor.client.plugins.resources.put
import io.ktor.client.request.post
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.ApplicationTestBuilder
import routes.PATH_TOKENS
import kotlin.random.Random

class UserHttpClient(val client: HttpClient, val userId: Long)

// class TokenHttpClient(val client: HttpClient, val projectId: Long, val userClient: UserHttpClient)

suspend fun ApplicationTestBuilder.createUserClient(
    block: (HttpClientConfig<out HttpClientEngineConfig>.() -> Unit)? = null
): UserHttpClient {
    val client = createClient {
        install(Resources)
        install(HttpCookies)
        block?.invoke(this)
    }

    val registerUserResponse = client.post(Users.Register()) {
        val random = Random.nextInt()
        setBodyForm(
            Users.NAME to "Test $random",
            Users.EMAIL to "test+$random@test.test",
            Users.PASSWORD to "password123$random"
        )
    }
    assert(registerUserResponse.status == HttpStatusCode.Created)

    val id = registerUserResponse.headers[HttpHeaders.Location]!!.substringAfterLast('/').toLong()
    return UserHttpClient(client = client, userId = id)
}

suspend fun UserHttpClient.withOrganization(role: RoleEnum = RoleEnum.ADMIN): Long {
    val createResponse = client.post(Organizations()) {
        setBodyForm(Organizations.NAME to "Test ${Random.nextInt()}")
    }
    assert(createResponse.status == HttpStatusCode.Created)

    val id = createResponse.headers[HttpHeaders.Location]!!.substringAfterLast('/').toLong()
    val updateResponse = client.put(Organizations.ById.Members.ById(id, userId)) {
        setBodyForm(Organizations.ROLE to role)
    }
    assert(updateResponse.status == HttpStatusCode.NoContent)

    return id.toLong()
}

suspend fun UserHttpClient.withProject(organizationId: Long): Long {
    val response = client.post("/organizations/$organizationId$PATH_PROJECTS") {
        setBodyForm("organization_id" to organizationId, "name" to "Test ${Random.nextInt()}")
    }
    assert(response.status == HttpStatusCode.Created)

    val id = response.headers[HttpHeaders.Location]!!.substringAfterLast('/')
    return id.toLong()
}

suspend fun UserHttpClient.withFlag(projectId: Long): Long {
    val response = client.post("${PATH_PROJECT(projectId)}$PATH_FLAGS") {
        setBodyForm("name" to "test-${Random.nextInt()}", "rule" to "true")
    }
    assert(response.status == HttpStatusCode.Created)

    val id = response.headers[HttpHeaders.Location]!!.substringAfterLast('/')
    return id.toLong()
}

suspend fun UserHttpClient.withToken(projectId: Long, permission: Permission): String {
    val response = client.post("${PATH_PROJECT(projectId)}$PATH_TOKENS") {
        setBodyForm(
            "project_id" to projectId,
            "permission" to permission,
            "description" to permission.toString().lowercase()
        )
    }
    assert(response.status == HttpStatusCode.Created)
    return response.body()
}

// suspend fun ApplicationTestBuilder.createTokenClient(
//     permission: Permission,
//     block: (HttpClientConfig<out HttpClientEngineConfig>.() -> Unit)? = null
// ): TokenHttpClient {
//     val userClient = createUserClient {
//         block?.invoke(this)
//     }
//     val organizationId = userClient.withOrganization()
//     val projectId = userClient.withProject(organizationId)
//
//     val createTokenResponse = userClient.client.post("${PATH_PROJECT(projectId)}$PATH_TOKENS") {
//         setBodyForm(
//             "project_id" to projectId,
//             "permission" to permission,
//             "description" to permission.toString().lowercase()
//         )
//     }
//     assert(createTokenResponse.status == HttpStatusCode.Created)
//     val token = createTokenResponse.body<String>()
//
//     return TokenHttpClient(
//         client = createClient {
//             install(Resources)
//             install(DefaultRequest) {
//                 header(HttpHeaders.Authorization, "Bearer $token")
//             }
//         },
//         projectId = projectId,
//         userClient = userClient
//     )
// }
