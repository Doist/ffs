package doist.ffs.components

import com.soywiz.krypto.md5
import doist.ffs.KEY_USER
import doist.ffs.api
import doist.ffs.components.reactist.Avatar
import doist.ffs.components.reactist.AvatarUser
import doist.ffs.components.reactist.ButtonLink
import doist.ffs.components.reactist.Inline
import doist.ffs.contexts.SessionContext
import doist.ffs.db.User
import doist.ffs.logout
import doist.ffs.use
import kotlinx.browser.localStorage
import react.FC
import react.Props
import react.router.dom.Link
import react.router.useNavigate
import react.useContext
import react.useState

val AuthMenu = FC<Props> {
    val (session, setSession) = useContext(SessionContext)
    val (user, _) = localStorage.use(KEY_USER, User.serializer())
    val navigate = useNavigate()
    var isSubmitting by useState(false)

    Inline {
        width = "fitContent"
        space = "small"
        alignY = "center"

        if (session != null) {
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
            }

            ButtonLink {
                variant = "tertiary"
                loading = isSubmitting
                onClick = {
                    isSubmitting = true
                    api(setSession) {
                        logout()
                        setSession(null)
                        isSubmitting = false
                        navigate(to = "/")
                    }.invokeOnCompletion { error ->
                        if (error != null) {
                            isSubmitting = false
                        }
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
