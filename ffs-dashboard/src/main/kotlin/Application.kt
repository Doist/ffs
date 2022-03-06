package doist.ffs

import doist.ffs.components.App
import doist.ffs.components.AuthForm
import doist.ffs.components.Landing
import doist.ffs.components.Project
import doist.ffs.components.Root
import kotlinx.browser.document
import kotlinx.js.jso
import react.FC
import react.Props
import react.create
import react.createElement
import react.dom.render
import react.router.Route
import react.router.Routes
import react.router.dom.BrowserRouter

fun main() {
    kotlinext.js.require("@doist/reactist/styles/reactist.css")
    render(root.create(), document.getElementById("root")!!)
}

val root = FC<Props> {
    BrowserRouter {
        Routes {
            Route {
                path = "/"
                element = createElement(Root)

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
                    element = createElement(Landing)
                }

                Route {
                    path = "app"
                    element = createElement(App)

                    Route {
                        path = "projects"

                        Route {
                            path = ":projectId"
                            element = createElement(Project)
                        }
                    }
                }
            }
        }
    }
}
