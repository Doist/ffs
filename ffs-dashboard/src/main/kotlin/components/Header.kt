package doist.ffs.components

import doist.ffs.components.reactist.Column
import doist.ffs.components.reactist.Columns
import react.FC
import react.Props

val Header = FC<Props> {
    Columns {
        Column {
            HomeLogo()
        }

        Column {
            width = "content"

            AuthMenu()
        }
    }
}
