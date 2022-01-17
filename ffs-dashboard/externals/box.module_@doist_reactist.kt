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

external interface BoxPaddingProps {
    var padding: dynamic /* "xsmall" | "small" | "medium" | "large" | "xlarge" | "xxlarge" | Readonly<Any>? */
        get() = definedExternally
        set(value) = definedExternally
    var paddingX: dynamic /* "xsmall" | "small" | "medium" | "large" | "xlarge" | "xxlarge" | Readonly<Any>? */
        get() = definedExternally
        set(value) = definedExternally
    var paddingY: dynamic /* "xsmall" | "small" | "medium" | "large" | "xlarge" | "xxlarge" | Readonly<Any>? */
        get() = definedExternally
        set(value) = definedExternally
    var paddingTop: dynamic /* "xsmall" | "small" | "medium" | "large" | "xlarge" | "xxlarge" | Readonly<Any>? */
        get() = definedExternally
        set(value) = definedExternally
    var paddingRight: dynamic /* "xsmall" | "small" | "medium" | "large" | "xlarge" | "xxlarge" | Readonly<Any>? */
        get() = definedExternally
        set(value) = definedExternally
    var paddingBottom: dynamic /* "xsmall" | "small" | "medium" | "large" | "xlarge" | "xxlarge" | Readonly<Any>? */
        get() = definedExternally
        set(value) = definedExternally
    var paddingLeft: dynamic /* "xsmall" | "small" | "medium" | "large" | "xlarge" | "xxlarge" | Readonly<Any>? */
        get() = definedExternally
        set(value) = definedExternally
}

external interface BoxMarginProps {
    var margin: dynamic /* "xsmall" | "small" | "medium" | "large" | "xlarge" | "xxlarge" | "-xsmall" | "-small" | "-medium" | "-large" | "-xlarge" | "-xxlarge" | Readonly<Any>? */
        get() = definedExternally
        set(value) = definedExternally
    var marginX: dynamic /* "xsmall" | "small" | "medium" | "large" | "xlarge" | "xxlarge" | "-xsmall" | "-small" | "-medium" | "-large" | "-xlarge" | "-xxlarge" | Readonly<Any>? */
        get() = definedExternally
        set(value) = definedExternally
    var marginY: dynamic /* "xsmall" | "small" | "medium" | "large" | "xlarge" | "xxlarge" | "-xsmall" | "-small" | "-medium" | "-large" | "-xlarge" | "-xxlarge" | Readonly<Any>? */
        get() = definedExternally
        set(value) = definedExternally
    var marginTop: dynamic /* "xsmall" | "small" | "medium" | "large" | "xlarge" | "xxlarge" | "-xsmall" | "-small" | "-medium" | "-large" | "-xlarge" | "-xxlarge" | Readonly<Any>? */
        get() = definedExternally
        set(value) = definedExternally
    var marginRight: dynamic /* "xsmall" | "small" | "medium" | "large" | "xlarge" | "xxlarge" | "-xsmall" | "-small" | "-medium" | "-large" | "-xlarge" | "-xxlarge" | Readonly<Any>? */
        get() = definedExternally
        set(value) = definedExternally
    var marginBottom: dynamic /* "xsmall" | "small" | "medium" | "large" | "xlarge" | "xxlarge" | "-xsmall" | "-small" | "-medium" | "-large" | "-xlarge" | "-xxlarge" | Readonly<Any>? */
        get() = definedExternally
        set(value) = definedExternally
    var marginLeft: dynamic /* "xsmall" | "small" | "medium" | "large" | "xlarge" | "xxlarge" | "-xsmall" | "-small" | "-medium" | "-large" | "-xlarge" | "-xxlarge" | Readonly<Any>? */
        get() = definedExternally
        set(value) = definedExternally
}

external interface BorderProps {
    var borderRadius: String? /* "standard" | "none" | "full" */
        get() = definedExternally
        set(value) = definedExternally
    var border: String? /* "primary" | "secondary" | "tertiary" | "none" */
        get() = definedExternally
        set(value) = definedExternally
}

external interface ReusableBoxProps : BorderProps, BoxPaddingProps {
    var minWidth: dynamic /* 0 | "xsmall" | "small" | "medium" | "large" | "xlarge" */
        get() = definedExternally
        set(value) = definedExternally
    var maxWidth: String? /* "xsmall" | "small" | "medium" | "large" | "xlarge" | "full" */
        get() = definedExternally
        set(value) = definedExternally
    var width: dynamic /* 0 | "xsmall" | "small" | "medium" | "large" | "xlarge" | "full" | "auto" | "maxContent" | "minContent" | "fitContent" */
        get() = definedExternally
        set(value) = definedExternally
    var background: String? /* "default" | "aside" | "highlight" | "selected" */
        get() = definedExternally
        set(value) = definedExternally
    var flexGrow: Number? /* 0 | 1 */
        get() = definedExternally
        set(value) = definedExternally
    var flexShrink: Number? /* 0 */
        get() = definedExternally
        set(value) = definedExternally
}

