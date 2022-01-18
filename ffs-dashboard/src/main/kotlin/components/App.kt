package doist.ffs.components

import doist.ffs.components.reactist.Column
import doist.ffs.components.reactist.Columns
import react.FC
import react.Props
import react.router.Outlet

val App = FC<Props> {
    Columns {
        space = "xxlarge"

        Column {
            width = "content"

            Organizations()
        }

        Column {
            Outlet()
        }
    }
}
