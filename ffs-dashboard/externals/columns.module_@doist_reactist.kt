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

external interface ColumnProps {
    var width: String? /* "auto" | "content" | "1/2" | "1/3" | "2/3" | "1/4" | "3/4" | "1/5" | "2/5" | "3/5" | "4/5" */
        get() = definedExternally
        set(value) = definedExternally
}

external var Column: Any

typealias ColumnsCollapseBelow = Any

external interface ColumnsProps : ReusableBoxProps {
    var space: dynamic /* "xsmall" | "small" | "medium" | "large" | "xlarge" | "xxlarge" | Readonly<Any>? */
        get() = definedExternally
        set(value) = definedExternally
    var align: dynamic /* "left" | "center" | "right" | Readonly<Any>? */
        get() = definedExternally
        set(value) = definedExternally
    var alignY: dynamic /* "top" | "center" | "bottom" | "baseline" | Readonly<Any>? */
        get() = definedExternally
        set(value) = definedExternally
    var collapseBelow: String? /* "mobile" | "tablet" | "desktop" */
        get() = definedExternally
        set(value) = definedExternally
}

external var Columns: Any