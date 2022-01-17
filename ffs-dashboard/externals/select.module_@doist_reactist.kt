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

external interface Option {
    var key: dynamic /* String? | Number? */
        get() = definedExternally
        set(value) = definedExternally
    var value: dynamic /* String | Number */
        get() = definedExternally
        set(value) = definedExternally
    var text: dynamic /* String? | Number? */
        get() = definedExternally
        set(value) = definedExternally
    var disabled: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

external interface Props {
    var className: String?
        get() = definedExternally
        set(value) = definedExternally
    var disabled: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var value: dynamic /* String? | Number? */
        get() = definedExternally
        set(value) = definedExternally
    var onChange: ((value: String) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var options: Array<Option>?
        get() = definedExternally
        set(value) = definedExternally
    var defaultValue: dynamic /* String? | Number? */
        get() = definedExternally
        set(value) = definedExternally
}

external fun Select(__0: Props): JSX.Element