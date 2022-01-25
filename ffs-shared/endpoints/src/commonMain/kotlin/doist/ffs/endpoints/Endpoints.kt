@file:Suppress("FunctionParameterNaming", "ConstructorParameterNaming", "FunctionNaming")

package doist.ffs.endpoints

import io.ktor.resources.Resource
import kotlinx.serialization.Serializable

object AuthScheme {
    const val Token = "Token"
    const val Session = "Session"
}

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
            @Resource("{userId}")
            data class ById(val parent: Members, val userId: Long)

            companion object {
                const val ROLE = "role"
                const val USER_ID = "user_id"
            }
        }

        @Serializable
        @Resource("projects")
        data class Projects(val parent: ById)
    }

    companion object {
        const val NAME = "name"

        fun ById.Members.Companion.ById(id: Long, userId: Long) = ById.Members.ById(
            ById.Members(ById(id = id)),
            userId = userId
        )

        fun ById.Companion.Projects(organizationId: Long) = ById.Projects(ById(id = organizationId))
    }
}

@Serializable
@Resource("projects")
class Projects {

    @Serializable
    @Resource("{id}")
    data class ById(val parent: Projects = Projects(), val id: Long) {

        @Serializable
        @Resource("tokens")
        data class Tokens(val parent: ById)

        @Serializable
        @Resource("flags")
        data class Flags(val parent: ById)
    }

    companion object {
        const val NAME = "name"

        fun ById.Companion.Tokens(projectId: Long) = ById.Tokens(ById(id = projectId))

        fun ById.Companion.Flags(projectId: Long) = ById.Flags(ById(id = projectId))
    }
}

@Serializable
@Resource("tokens")
class Tokens {

    @Serializable
    @Resource("{id}")
    data class ById(val parent: Tokens = Tokens(), val id: Long)

    companion object {
        const val PERMISSION = "permission"
        const val DESCRIPTION = "description"
    }
}

@Serializable
@Resource("flags")
class Flags {

    @Serializable
    @Resource("{id}")
    data class ById(val parent: Flags = Flags(), val id: Long) {

        @Serializable
        @Resource("archive")
        data class Archive(val parent: ById)
    }

    @Serializable
    @Resource("eval")
    data class Eval(val parent: Flags = Flags())

    companion object {
        const val NAME = "name"
        const val RULE = "rule"
        const val ENV = "env"

        fun ById.Companion.Archive(id: Long) = ById.Archive(ById(id = id))
    }
}
