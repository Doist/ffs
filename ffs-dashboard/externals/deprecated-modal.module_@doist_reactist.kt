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
    var className: String?
        get() = definedExternally
        set(value) = definedExternally
    var style: Any?
        get() = definedExternally
        set(value) = definedExternally
    var large: Boolean
    var medium: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var closeOnOverlayClick: Boolean
}

external open class Box : React.Component<React.PropsWithChildren<Props>> {
    open fun componentDidMount()
    open fun componentWillUnmount()
    open var _closeModal: () -> Unit
    open var _handleKeyDown: (event: Any) -> Unit
    open var _handleOverlayClick: (event: React.MouseEvent<Element>) -> Unit
    open fun render(): JSX.Element

    companion object {
        var displayName: String
        var defaultProps: Props
    }
}

external interface HeaderProps {
    var title: dynamic /* String? | React.ReactNode? */
        get() = definedExternally
        set(value) = definedExternally
    var subtitle: dynamic /* String? | React.ReactNode? */
        get() = definedExternally
        set(value) = definedExternally
    var beforeClose: (() -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
}

external open class Header : React.Component<HeaderProps> {
    open fun _closeModal(event: React.MouseEvent)
    open fun render(): JSX.Element

    companion object {
        var displayName: String
        var defaultProps: HeaderProps
    }
}

external interface BodyProps {
    var icon: Any?
        get() = definedExternally
        set(value) = definedExternally
    var plain: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var style: Any?
        get() = definedExternally
        set(value) = definedExternally
    var className: String?
        get() = definedExternally
        set(value) = definedExternally
    var showCloseIcon: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

external open class Body : React.Component<BodyProps> {
    open fun _closeModal(event: React.MouseEvent)
    open fun render(): JSX.Element

    companion object {
        var displayName: String
        var defaultProps: BodyProps
    }
}

external interface ActionProps {
    var children: Any?
        get() = definedExternally
        set(value) = definedExternally
}

external interface ActionChildrenProps {
    var close: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var onClick: (() -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
}

external open class Actions : React.Component<ActionProps> {
    open fun _onClick(onClick: () -> Unit)
    open fun render(): JSX.Element

    companion object {
        var displayName: String
    }
}

external interface Modal {
    var Box: Box
    var Header: Header
    var Body: Body
    var Actions: Actions
}