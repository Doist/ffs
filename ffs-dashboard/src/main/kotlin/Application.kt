package doist.ffs

import doist.ffs.components.App
import doist.ffs.components.AuthForm
import doist.ffs.components.Dashboard
import kotlinext.js.jso
import kotlinx.browser.document
import react.FC
import react.Props
import react.create
import react.createElement
import react.dom.render
import react.router.Route
import react.router.Routes
import react.router.dom.BrowserRouter

@Suppress("TooGenericExceptionCaught", "PrintStackTrace")
fun main() {
    kotlinext.js.require("@doist/reactist/styles/reactist.css")
    render(root.create(), document.getElementById("root")!!)
}

val root = FC<Props> {
    BrowserRouter {
        Routes {
            Route {
                path = "/"
                element = createElement(App)

                Route {
                    path = "login"
                    element = createElement(AuthForm)
                }

                Route {
                    path = "register"
                    element = createElement(
                        AuthForm,
                        props = jso {
                            register = true
                        }
                    )
                }

                Route {
                    index = true
                    element = createElement(Dashboard)
                }
            }
        }
    }
}
