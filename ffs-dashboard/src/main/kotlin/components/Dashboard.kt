package doist.ffs.components

import doist.ffs.components.reactist.Column
import doist.ffs.components.reactist.Columns
import doist.ffs.contexts.SessionContext
import react.FC
import react.Props
import react.router.Outlet
import react.useContext

val Dashboard = FC<Props> {
    val (session, _) = useContext(SessionContext)
    if (session == null) {
        return@FC
    }

    Columns {
        space = "xxlarge"

        Column {
            width = "content"
        }

        Column {
            Outlet()
        }
    }
}
