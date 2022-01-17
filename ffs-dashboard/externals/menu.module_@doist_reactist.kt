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
import tsstdlib.Omit
import tsstdlib.Pick

typealias NativeProps<E> = React.DetailedHTMLProps<React.HTMLAttributes<E>, E>

external fun Menu(__0: Omit<unstable_IdInitialState /* unstable_IdInitialState & Any */, String /* "visible" */> /* Omit<unstable_IdInitialState /* unstable_IdInitialState & Any */, String /* "visible" */> & `T$47` */): JSX.Element

external interface MenuItemProps {
    var value: String?
        get() = definedExternally
        set(value) = definedExternally
    var children: Any
    var disabled: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var hideOnSelect: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var onSelect: (() -> Any)?
        get() = definedExternally
        set(value) = definedExternally
    var onClick: ((event: React.MouseEvent) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
}

typealias SubMenuProps = Pick<Omit<unstable_IdInitialState /* unstable_IdInitialState & Any */, String /* "visible" */> /* Omit<unstable_IdInitialState /* unstable_IdInitialState & Any */, String /* "visible" */> & `T$47` */, String /* "children" | "onItemSelect" */>