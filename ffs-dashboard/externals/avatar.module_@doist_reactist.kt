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

external interface `T$42` {
    var name: String?
        get() = definedExternally
        set(value) = definedExternally
    var email: String
}

external interface Props {
    var className: String?
        get() = definedExternally
        set(value) = definedExternally
    var colorList: Array<String>?
        get() = definedExternally
        set(value) = definedExternally
    var size: String? /* "xxs" | "xs" | "s" | "m" | "l" | "xl" | "xxl" | "xxxl" */
        get() = definedExternally
        set(value) = definedExternally
    var avatarUrl: String?
        get() = definedExternally
        set(value) = definedExternally
    var user: `T$42`
}

external fun Avatar(__0: Props): JSX.Element