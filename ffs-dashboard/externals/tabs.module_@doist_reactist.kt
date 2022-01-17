@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

import kotlin.js.*
import org.khronos.webgl.*
import org.w3c.dom.*
import org.w3c.dom.events.*
import org.w3c.dom.parsing.*
import org.w3c.dom.svg.*
import org.w3c.dom.url.*
import org.w3c.fetch.*
import org.w3c.files.*
import org.w3c.notifications.*
import org.w3c.performance.*
import org.w3c.workers.*
import org.w3c.xhr.*

external interface TabsProps {
    var children: Any
    var color: String? /* "primary" | "secondary" | "tertiary" */
        get() = definedExternally
        set(value) = definedExternally
    var variant: String? /* "normal" | "plain" */
        get() = definedExternally
        set(value) = definedExternally
    var selectedId: String?
        get() = definedExternally
        set(value) = definedExternally
}

external fun Tabs(__0: TabsProps): React.ReactElement

external interface TabProps {
    var children: Any
    var id: String
}

external fun Tab(__0: TabProps): React.ReactElement?

external fun TabList(__0: Any /* `T$34` | `T$35` | `T$36` */): React.ReactElement?

external interface TabPanelProps {
    var children: Any
    var id: String
    var render: String? /* "always" | "active" | "lazy" */
        get() = definedExternally
        set(value) = definedExternally
}

external var TabPanel: Any

external interface `T$33` {
    var selectedId: String?
        get() = definedExternally
        set(value) = definedExternally
}

external interface TabAwareSlotProps {
    var children: (provided: `T$33`) -> React.ReactElement?
}

external fun TabAwareSlot(__0: TabAwareSlotProps): React.ReactElement?