@file:Suppress("FunctionParameterNaming", "ConstructorParameterNaming", "FunctionNaming")

package doist.ffs.endpoints

import io.ktor.resources.Resource
import kotlinx.serialization.Serializable

@Serializable
@Resource("users")
class Users {
    @Serializable
    @Resource("register")
    data class Register(val parent: Users = Users())

    @Serializable
    @Resource("login")
    data class Login(val parent: Users = Users())

    @Serializable
    @Resource("logout")
    data class Logout(val parent: Users = Users())

    @Serializable
    @Resource("{id}")
    data class ById(val parent: Users = Users(), val id: Long)

    companion object {
        const val NAME = "name"
        const val EMAIL = "email"
        const val PASSWORD = "password"
        const val CURRENT_PASSWORD = "current_password"
    }
}

@Serializable
@Resource("organizations")
class Organizations {
    @Serializable
    @Resource("{id}")
    data class ById(val parent: Organizations = Organizations(), val id: Long) {

        @Serializable
        @Resource("members")
        data class Members(val parent: Organizations.ById) {

            @Serializable
            @Resource("{user_id}")
            data class ById(val parent: Members, val user_id: Long)
        }
    }

    companion object {
        const val NAME = "name"
        const val ROLE = "role"
        const val USER_ID = "user_id"

        fun ById.Members.Companion.ById(id: Long, user_id: Long) = ById.Members.ById(
            ById.Members(ById(id = id)),
            user_id = user_id
        )
    }
}
