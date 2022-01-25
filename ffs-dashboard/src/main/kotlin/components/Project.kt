package doist.ffs.components

import doist.ffs.components.reactist.Tab
import doist.ffs.components.reactist.TabList
import doist.ffs.components.reactist.TabPanel
import doist.ffs.components.reactist.Tabs
import react.FC
import react.Props
import react.dom.aria.ariaLabel
import react.router.useParams

val Project = FC<Props> {
    val params = useParams()
    val projectId = params["projectId"]!!.toLong()

    Tabs {
        color = "tertiary"
        this.selectedId = selectedId ?: "rules"

        TabList {
            ariaLabel = "Project tabs"

            Tab {
                id = "rules"
                +"Rules"
            }

            Tab {
                id = "tokens"
                +"Tokens"
            }
        }

        TabPanel {
            id = "rules"

            Flags {
                this.projectId = projectId
            }
        }

        TabPanel {
            id = "tokens"

            Tokens {
                this.projectId = projectId
            }
        }
    }
}
