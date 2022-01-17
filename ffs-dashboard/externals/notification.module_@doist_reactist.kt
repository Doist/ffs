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

external interface `T$46` {
    @nativeGetter
    operator fun get(key: String): String? /* "off" | "polite" | "assertive" */
    @nativeSetter
    operator fun set(key: String, value: String? /* "off" | "polite" | "assertive" */)
    var id: String
    var icon: Any?
        get() = definedExternally
        set(value) = definedExternally
    var title: Any?
        get() = definedExternally
        set(value) = definedExternally
    var subtitle: Any?
        get() = definedExternally
        set(value) = definedExternally
    var children: Any?
        get() = definedExternally
        set(value) = definedExternally
    var customCloseButton: Any?
        get() = definedExternally
        set(value) = definedExternally
    var onClick: ((event: React.MouseEvent<HTMLButtonElement, MouseEvent>) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var onClose: ((event: React.MouseEvent<HTMLButtonElement, MouseEvent>) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var closeAltText: String?
        get() = definedExternally
        set(value) = definedExternally
    var className: String?
        get() = definedExternally
        set(value) = definedExternally
}

external fun Notification(__0: `T$46` /* `T$46` & Omit<Any, String /* "aria-live" */> */): JSX.Element