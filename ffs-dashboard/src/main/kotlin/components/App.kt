package doist.ffs.components

import doist.ffs.KEY_SESSION
import doist.ffs.components.reactist.Stack
import doist.ffs.contexts.SessionContext
import doist.ffs.use
import kotlinx.browser.localStorage
import react.FC
import react.Props
import react.router.Outlet
import react.useEffect

val App = FC<Props> {
    val sessionState = localStorage.use(KEY_SESSION)
    val (session, _) = sessionState

    useEffect(session) {
        if (session == null) {
            localStorage.clear()
        }
    }

    SessionContext.Provider(value = sessionState) {
        Stack {
            maxWidth = "large"
            paddingTop = "medium"
            space = "xxlarge"

            Header()

            Outlet()
        }
    }
}
