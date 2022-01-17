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
import tsstdlib.ReturnType

external interface Props {
    var time: Number?
        get() = definedExternally
        set(value) = definedExternally
    var config: TimeConfig?
        get() = definedExternally
        set(value) = definedExternally
    var className: String?
        get() = definedExternally
        set(value) = definedExternally
    var tooltipOnHover: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var refresh: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var tooltip: Any?
        get() = definedExternally
        set(value) = definedExternally
    var expandOnHover: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var expandFullyOnHover: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

external interface State {
    var hovered: Boolean
    var mouseX: Number?
        get() = definedExternally
        set(value) = definedExternally
    var mouseY: Number?
        get() = definedExternally
        set(value) = definedExternally
}

external open class Time(props: Props) : React.Component<Props, State> {
    open fun componentDidMount()
    open fun componentDidUpdate(prevProps: Props)
    open fun componentWillUnmount()
    open var refreshInterval: ReturnType<Any>
    open fun _setHovered(hovered: Boolean, event: React.MouseEvent)
    open fun _renderTime(config: TimeConfig): String?
    open fun _refresh()
    open fun render(): JSX.Element

    companion object {
        var displayName: String
        var defaultProps: Props
    }
}