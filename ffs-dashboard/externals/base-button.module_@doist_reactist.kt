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

typealias IconElement = React.ReactChild

external interface CommonProps {
    var variant: String /* "primary" | "secondary" | "tertiary" | "quaternary" */
    var tone: String? /* "normal" | "destructive" */
        get() = definedExternally
        set(value) = definedExternally
    var size: String? /* "small" | "normal" | "large" */
        get() = definedExternally
        set(value) = definedExternally
    var disabled: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var loading: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var tooltip: Any
        get() = definedExternally
        set(value) = definedExternally
}

external interface IconButtonProps {
    @nativeGetter
    operator fun get(key: String): String?
    @nativeSetter
    operator fun set(key: String, value: String)
    var icon: IconElement
    var children: Any?
        get() = definedExternally
        set(value) = definedExternally
    var startIcon: Any?
        get() = definedExternally
        set(value) = definedExternally
    var endIcon: Any?
        get() = definedExternally
        set(value) = definedExternally
}

external interface LabelledButtonProps {
    var children: Any
    var startIcon: IconElement?
        get() = definedExternally
        set(value) = definedExternally
    var endIcon: IconElement?
        get() = definedExternally
        set(value) = definedExternally
    var icon: Any?
        get() = definedExternally
        set(value) = definedExternally
}