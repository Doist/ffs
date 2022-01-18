@file:Suppress("MaxLineLength", "VariableNaming")
@file:JsModule("@doist/reactist")
@file:JsNonModule
package doist.ffs.components.reactist

import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLInputElement
import react.FC
import react.Props
import react.PropsWithClassName
import react.PropsWithStyle
import react.dom.html.ButtonHTMLAttributes
import react.dom.html.InputHTMLAttributes

external interface StandardProps : PropsWithStyle, PropsWithClassName {
    var `as`: dynamic
        get() = definedExternally
        set(value) = definedExternally
}

external interface AvatarUser {
    var name: String?
        get() = definedExternally
        set(value) = definedExternally
    var email: String
}

external interface AvatarProps : StandardProps {
    var colorList: Array<String>?
        get() = definedExternally
        set(value) = definedExternally
    var size: String? /* "xxs" | "xs" | "s" | "m" | "l" | "xl" | "xxl" | "xxxl" */
        get() = definedExternally
        set(value) = definedExternally
    var avatarUrl: String?
        get() = definedExternally
        set(value) = definedExternally
    var user: AvatarUser
}

external val Avatar: FC<AvatarProps> = definedExternally

external interface BoxPaddingProps : StandardProps {
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

external interface BoxMarginProps : StandardProps {
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

external interface BorderProps : StandardProps {
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

external interface BoxProps : ReusableBoxProps, BoxMarginProps {
    var position: dynamic /* "absolute" | "fixed" | "relative" | "static" | "sticky" | Readonly<Any>? */
        get() = definedExternally
        set(value) = definedExternally
    var display: dynamic /* "block" | "flex" | "inline" | "inlineBlock" | "inlineFlex" | "none" | Readonly<Any>? */
        get() = definedExternally
        set(value) = definedExternally
    var flexDirection: dynamic /* "column" | "row" | Readonly<Any>? */
        get() = definedExternally
        set(value) = definedExternally
    var flexWrap: String? /* "nowrap" | "wrap" */
        get() = definedExternally
        set(value) = definedExternally
    var alignItems: dynamic /* "center" | "flexEnd" | "flexStart" | "baseline" | Readonly<Any>? */
        get() = definedExternally
        set(value) = definedExternally
    var justifyContent: dynamic /* "center" | "flexEnd" | "flexStart" | "spaceAround" | "spaceBetween" | "spaceEvenly" | Readonly<Any>? */
        get() = definedExternally
        set(value) = definedExternally
    var overflow: String? /* "hidden" | "auto" | "visible" | "scroll" */
        get() = definedExternally
        set(value) = definedExternally
    var height: String? /* "full" */
        get() = definedExternally
        set(value) = definedExternally
    var textAlign: dynamic /* "start" | "center" | "end" | "justify" | Readonly<Any>? */
        get() = definedExternally
        set(value) = definedExternally
}

external var Box: Any

external interface InlineProps : ReusableBoxProps {
    var space: dynamic /* "xsmall" | "small" | "medium" | "large" | "xlarge" | "xxlarge" | Readonly<Any>? */
        get() = definedExternally
        set(value) = definedExternally
    var align: dynamic /* "left" | "center" | "right" | Readonly<Any>? */
        get() = definedExternally
        set(value) = definedExternally
    var alignY: dynamic /* "top" | "center" | "bottom" | Readonly<Any>? */
        get() = definedExternally
        set(value) = definedExternally
}

external var Inline: FC<InlineProps> = definedExternally

external interface ColumnProps : StandardProps {
    var width: String? /* "auto" | "content" | "1/2" | "1/3" | "2/3" | "1/4" | "3/4" | "1/5" | "2/5" | "3/5" | "4/5" */
        get() = definedExternally
        set(value) = definedExternally
}

external var Column: FC<ColumnProps> = definedExternally

external interface ColumnsProps : ReusableBoxProps {
    var space: dynamic /* "xsmall" | "small" | "medium" | "large" | "xlarge" | "xxlarge" | Readonly<Any>? */
        get() = definedExternally
        set(value) = definedExternally
    var align: dynamic /* "left" | "center" | "right" | Readonly<Any>? */
        get() = definedExternally
        set(value) = definedExternally
    var alignY: dynamic /* "top" | "center" | "bottom" | "baseline" | Readonly<Any>? */
        get() = definedExternally
        set(value) = definedExternally
    var collapseBelow: String? /* "mobile" | "tablet" | "desktop" */
        get() = definedExternally
        set(value) = definedExternally
}

external var Columns: FC<ColumnsProps> = definedExternally

external interface StackProps : ReusableBoxProps {
    var space: dynamic /* "xsmall" | "small" | "medium" | "large" | "xlarge" | "xxlarge" | Readonly<Any>? */
        get() = definedExternally
        set(value) = definedExternally
    var align: dynamic /* "start" | "center" | "end" | Readonly<Any>? */
        get() = definedExternally
        set(value) = definedExternally
    var dividers: String? /* "primary" | "secondary" | "tertiary" | "none" */
        get() = definedExternally
        set(value) = definedExternally
}

external var Stack: FC<StackProps> = definedExternally

external interface BaseFieldProps : StandardProps, InputHTMLAttributes<HTMLInputElement> {
    var label: dynamic
        get() = definedExternally
        set(value) = definedExternally
    var secondaryLabel: dynamic
        get() = definedExternally
        set(value) = definedExternally
    var auxiliaryLabel: dynamic
        get() = definedExternally
        set(value) = definedExternally
    var hint: dynamic
        get() = definedExternally
        set(value) = definedExternally
    var maxWidth: dynamic
        get() = definedExternally
        set(value) = definedExternally
}

external interface TextFieldProps : BaseFieldProps {
    override var type: dynamic /* "email" | "search" | "tel" | "text" | "url" */
        get() = definedExternally
        set(value) = definedExternally
}

external var TextField: FC<TextFieldProps> = definedExternally

external interface PasswordFieldProps : BaseFieldProps {
    var togglePasswordLabel: String?
        get() = definedExternally
        set(value) = definedExternally
}

external var PasswordField: FC<PasswordFieldProps> = definedExternally

external interface CommonButtonProps : StandardProps, ButtonHTMLAttributes<HTMLButtonElement> {
    var variant: String /* "primary" | "secondary" | "tertiary" | "quaternary" */
    var tone: String? /* "normal" | "destructive" */
        get() = definedExternally
        set(value) = definedExternally
    var size: String? /* "small" | "normal" | "large" */
        get() = definedExternally
        set(value) = definedExternally
    var loading: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var tooltip: Any?
        get() = definedExternally
        set(value) = definedExternally
}

external interface IconButtonProps : StandardProps {
    var icon: IconElement?
        get() = definedExternally
        set(value) = definedExternally
}

external interface LabelledButtonProps : StandardProps {
    var startIcon: IconElement?
        get() = definedExternally
        set(value) = definedExternally
    var endIcon: IconElement?
        get() = definedExternally
        set(value) = definedExternally
}

external interface BaseButtonProps : CommonButtonProps, IconButtonProps, LabelledButtonProps

external interface ButtonProps : BaseButtonProps

external var Button: FC<ButtonProps> = definedExternally

external interface OpenInNewTab {
    var openInNewTab: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

external interface ButtonLinkProps : BaseButtonProps, OpenInNewTab {
    var href: String?
        get() = definedExternally
        set(value) = definedExternally
    var to: history.To?
        get() = definedExternally
        set(value) = definedExternally
}

external var ButtonLink: FC<ButtonLinkProps> = definedExternally
