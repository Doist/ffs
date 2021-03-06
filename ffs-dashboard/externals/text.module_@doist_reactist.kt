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

external interface TextProps {
    var children: Any
    var size: String? /* "caption" | "copy" | "body" | "subtitle" */
        get() = definedExternally
        set(value) = definedExternally
    var weight: String? /* "regular" | "semibold" | "bold" */
        get() = definedExternally
        set(value) = definedExternally
    var tone: String? /* "normal" | "secondary" | "danger" */
        get() = definedExternally
        set(value) = definedExternally
    var lineClamp: dynamic /* 1 | 2 | 3 | 4 | 5 | "1" | "2" | "3" | "4" | "5" */
        get() = definedExternally
        set(value) = definedExternally
    var align: dynamic /* "start" | "center" | "end" | "justify" | Readonly<Any>? */
        get() = definedExternally
        set(value) = definedExternally
}