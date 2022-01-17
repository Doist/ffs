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

typealias EventHandler = (event: React.SyntheticEvent) -> Unit

external interface EventHandlerProps {
    var onArrowUp: EventHandler?
        get() = definedExternally
        set(value) = definedExternally
    var onArrowDown: EventHandler?
        get() = definedExternally
        set(value) = definedExternally
    var onArrowLeft: EventHandler?
        get() = definedExternally
        set(value) = definedExternally
    var onArrowRight: EventHandler?
        get() = definedExternally
        set(value) = definedExternally
    var onEnter: EventHandler?
        get() = definedExternally
        set(value) = definedExternally
    var onBackspace: EventHandler?
        get() = definedExternally
        set(value) = definedExternally
    var onEscape: EventHandler?
        get() = definedExternally
        set(value) = definedExternally
}

external interface PropagateProps {
    var propagateArrowUp: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var propagateArrowDown: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var propagateArrowLeft: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var propagateArrowRight: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var propagateEnter: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var propagateBackspace: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var propagateEscape: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

external fun KeyCapturer(props: EventHandlerProps /* EventHandlerProps & PropagateProps & `T$45` */): React.ReactElement<Any, dynamic /* String | (props: Any) -> React.ReactElement<Any, dynamic /* String | Any */>? | Any */>