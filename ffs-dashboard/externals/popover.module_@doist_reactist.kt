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

external interface Props {
    var visible: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var popoverRef: Any?
        get() = definedExternally
        set(value) = definedExternally
    var wrapperRef: Any?
        get() = definedExternally
        set(value) = definedExternally
    var onMouseEnter: Any?
        get() = definedExternally
        set(value) = definedExternally
    var onMouseLeave: Any?
        get() = definedExternally
        set(value) = definedExternally
    var onClick: Any?
        get() = definedExternally
        set(value) = definedExternally
    var wrapperClassName: String?
        get() = definedExternally
        set(value) = definedExternally
    var popoverClassName: String?
        get() = definedExternally
        set(value) = definedExternally
    var arrowClassName: String?
        get() = definedExternally
        set(value) = definedExternally
    var content: dynamic /* (() -> React.ReactNode)? | React.ReactNode? */
        get() = definedExternally
        set(value) = definedExternally
    var trigger: Any?
        get() = definedExternally
        set(value) = definedExternally
    var position: String /* "left" | "right" | "top" | "bottom" | "vertical" | "horizontal" | "auto" */
    var withArrow: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var allowVaguePositioning: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var gapSize: Number
}

external open class Popover : React.Component<Props> {
    open fun componentDidMount()
    open fun componentDidUpdate(prevProps: Props)
    open var popover: HTMLElement
    open var wrapper: HTMLElement
    open var _updatePopoverPosition: () -> Unit
    open var _getClassNameForPosition: (position: String /* "left" | "right" | "top" | "bottom" | "vertical" | "horizontal" | "auto" */) -> String
    open var _updatePopoverRef: (popover: HTMLElement) -> Unit
    open var _updateWrapperRef: (wrapper: HTMLElement) -> Unit
    open fun render(): JSX.Element

    companion object {
        var displayName: String
        var defaultProps: Props
    }
}