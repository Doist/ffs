package doist.ffs.components

import com.soywiz.krypto.md5
import doist.ffs.api
import doist.ffs.components.reactist.Avatar
import doist.ffs.components.reactist.AvatarUser
import doist.ffs.components.reactist.ButtonLink
import doist.ffs.components.reactist.Inline
import doist.ffs.contexts.UserContext
import doist.ffs.logout
import react.FC
import react.Props
import react.router.dom.Link
import react.useContext
import react.useState

val AuthMenu = FC<Props> {
    val (user, setUser) = useContext(UserContext)
    var isSubmitting by useState(false)

    Inline {
        width = "fitContent"
        space = "small"
        alignY = "center"

        if (user != null) {
            Avatar {
                val hash = user.email.lowercase().encodeToByteArray().md5().hex
                avatarUrl = "https://www.gravatar.com/avatar/$hash?s=20&d=blank"
                size = "xs"
                this.user = AvatarUser().apply {
                    name = user.name
                    email = user.email
                }
            }

            ButtonLink {
                variant = "tertiary"
                loading = isSubmitting
                onClick = {
                    isSubmitting = true
                    api(setUser) {
                        logout()
                        setUser(null)
                    }.invokeOnCompletion {
                        isSubmitting = false
                    }
                }
                +"Logout"
            }
        } else {
            ButtonLink {
                variant = "quaternary"
                `as` = Link
                to = "/login"
                +"Login"
            }

            ButtonLink {
                variant = "primary"
                `as` = Link
                to = "/register"
                +"Register"
            }
        }
    }
}
