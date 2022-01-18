@file:Suppress("MaxLineLength", "VariableNaming", "Unused")
@file:JsModule("@doist/reactist")
@file:JsNonModule
package doist.ffs.components.reactist

import org.w3c.dom.HTMLAnchorElement
import react.FC
import react.Props
import react.PropsWithStyle
import react.ReactElement
import react.ReactNode
import react.dom.aria.AriaAttributes
import react.dom.html.AnchorHTMLAttributes

external interface StandardProps : PropsWithStyle, AriaAttributes {
    var `as`: dynamic
        get() = definedExternally
        set(value) = definedExternally
}

external interface ObfuscatedClassName {
    var exceptionallySetClassName: String?
        get() = definedExternally
        set(value) = definedExternally
}

external interface AvatarUser {
    var name: String?
        get() = definedExternally
        set(value) = definedExternally
    var email: String
}

external interface AvatarProps : StandardProps, ObfuscatedClassName {
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

external interface BoxProps : StandardProps, HtmlDivProps, ReusableBoxProps, BoxMarginProps {
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

external var Box: FC<BoxProps> = definedExternally

external interface InlineProps : StandardProps, HtmlDivProps, ReusableBoxProps {
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

external interface ColumnProps : StandardProps, HtmlDivProps {
    var width: String? /* "auto" | "content" | "1/2" | "1/3" | "2/3" | "1/4" | "3/4" | "1/5" | "2/5" | "3/5" | "4/5" */
        get() = definedExternally
        set(value) = definedExternally
}

external var Column: FC<ColumnProps> = definedExternally

external interface ColumnsProps : StandardProps, HtmlDivProps, ReusableBoxProps {
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

external interface StackProps : StandardProps, HtmlDivProps, ReusableBoxProps {
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

external interface BaseFieldProps : HtmlInputProps {
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

external interface TextFieldProps : StandardProps, BaseFieldProps {
    override var type: dynamic /* "email" | "search" | "tel" | "text" | "url" */
        get() = definedExternally
        set(value) = definedExternally
}

external var TextField: FC<TextFieldProps> = definedExternally

external interface PasswordFieldProps : StandardProps, BaseFieldProps {
    var togglePasswordLabel: String?
        get() = definedExternally
        set(value) = definedExternally
}

external var PasswordField: FC<PasswordFieldProps> = definedExternally

external interface CommonButtonProps {
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

external interface IconButtonProps {
    var icon: IconElement?
        get() = definedExternally
        set(value) = definedExternally
}

external interface LabelledButtonProps {
    var startIcon: IconElement?
        get() = definedExternally
        set(value) = definedExternally
    var endIcon: IconElement?
        get() = definedExternally
        set(value) = definedExternally
}

external interface BaseButtonProps : CommonButtonProps, IconButtonProps, LabelledButtonProps

external interface ButtonProps : StandardProps, HtmlButtonProps, BaseButtonProps

external var Button: FC<ButtonProps> = definedExternally

external interface OpenInNewTab {
    var openInNewTab: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

external interface ButtonLinkProps : StandardProps, HtmlAnchorProps, BaseButtonProps, OpenInNewTab {
    var to: history.To?
        get() = definedExternally
        set(value) = definedExternally
}

external var ButtonLink: FC<ButtonLinkProps> = definedExternally

external interface LoadingProps : StandardProps, HtmlDivProps, ObfuscatedClassName {
    var size: String? /* "small" | "medium" | "large" */
        get() = definedExternally
        set(value) = definedExternally
}

external var Loading: FC<LoadingProps> = definedExternally

external interface TextProps : HtmlDivProps {
    var size: String? /* "caption" | "copy" | "body" | "subtitle" */
        get() = definedExternally
        set(value) = definedExternally
    var weight: String? /* "regular" | "semibold" | "bold" */
        get() = definedExternally
        set(value) = definedExternally
    var tone: String? /* "normal" | "secondary" | "danger" */
        get() = definedExternally
        set(value) = definedExternally
    var lineClamp: dynamic /* 1 | 2 | 3 | 4 | 5 | "1" | "2" | "3" | "4" | "5" */
        get() = definedExternally
        set(value) = definedExternally
    var align: dynamic /* "start" | "center" | "end" | "justify" | Readonly<Any>? */
        get() = definedExternally
        set(value) = definedExternally
}

external var Text: FC<TextProps> = definedExternally

external interface TextLinkProps :
    StandardProps, AnchorHTMLAttributes<HTMLAnchorElement>, OpenInNewTab {

    var to: history.To?
        get() = definedExternally
        set(value) = definedExternally
}

external var TextLink: FC<TextLinkProps> = definedExternally

external interface TabsProps : Props {
    var children: ReactNode
    var color: String? /* "primary" | "secondary" | "tertiary" */
        get() = definedExternally
        set(value) = definedExternally
    var variant: String? /* "normal" | "plain" */
        get() = definedExternally
        set(value) = definedExternally
    var selectedId: String?
        get() = definedExternally
        set(value) = definedExternally
}

external var Tabs: FC<TabsProps> = definedExternally

external interface TabProps : Props {
    var children: ReactNode
    var id: String /* TabPanel id */
}

external var Tab: FC<TabProps> = definedExternally

external interface TabListProps : StandardProps, AriaAttributes {
    var children: ReactNode
    var space: dynamic /* "xsmall" | "small" | "medium" | "large" | "xlarge" | "xxlarge" | Readonly<Any>? */
        get() = definedExternally
        set(value) = definedExternally
}

external var TabList: FC<TabListProps> = definedExternally

external interface TabPanelProps : StandardProps, HtmlDivProps {
    var render: String? /* "always" | "active" | "lazy" */
        get() = definedExternally
        set(value) = definedExternally
}

external var TabPanel: FC<TabPanelProps> = definedExternally

external interface TabAwareSlotProvided {
    var selectedId: String?
        get() = definedExternally
        set(value) = definedExternally
}

external interface TabAwareSlotProps : Props {
    var children: (provided: TabAwareSlotProvided) -> ReactElement?
}

external var TabAwareSlot: FC<TabAwareSlotProps> = definedExternally

external interface NoticeProps : Props {
    var id: String?
        get() = definedExternally
        set(value) = definedExternally
    var children: ReactNode
    var tone: String /* "info" | "positive" | "caution" | "critical" */
}

external var Notice: FC<NoticeProps> = definedExternally

external interface SelectFieldProps : BaseFieldProps

external var SelectField: FC<SelectFieldProps> = definedExternally
