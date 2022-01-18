package doist.ffs.components

import doist.ffs.components.reactist.Stack
import doist.ffs.contexts.UserContext
import doist.ffs.models.User
import doist.ffs.use
import kotlinx.browser.localStorage
import react.FC
import react.Props
import react.router.Outlet

val App = FC<Props> {
    UserContext.Provider(value = localStorage.use("user", User.serializer())) {
        Stack {
            maxWidth = "large"
            paddingTop = "medium"
            space = "xxlarge"

            Header()

            Outlet()
        }
    }
}
