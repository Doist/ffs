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
