@file:Suppress("MatchingDeclarationName", "MaxLineLength")

package doist.ffs.components

import doist.ffs.KEY_SESSION
import doist.ffs.components.reactist.Box
import doist.ffs.components.reactist.Notice
import doist.ffs.components.reactist.Text
import doist.ffs.components.reactist.TextLink
import doist.ffs.use
import kotlinext.js.jso
import kotlinx.browser.localStorage
import react.FC
import react.Props
import react.router.useNavigate
import react.useEffect

val Landing = FC<Props> {
    val (session, _) = localStorage.use(KEY_SESSION)
    val navigate = useNavigate()

    useEffect {
        if (session != null) {
            navigate(
                to = "/app",
                options = jso {
                    replace = true
                }
            )
        }
    }

    Box {
        paddingTop = "xxlarge"
        maxWidth = "large"

        Notice {
            tone = "caution"

            Text {
                +"FFS is in active development. This instance is not persistent and exists for demonstration purposes only. Don't rely on it."
            }

            Text {
                +"Meanwhile, get involved! "

                TextLink {
                    href = "https://github.com/Doist/ffs/issues"
                    +"Contributions welcome. "
                }

                +"Documentation available "

                TextLink {
                    href = "https://doist.github.io/ffs/"
                    +"here"
                }

                +"."
            }
        }
    }
}
