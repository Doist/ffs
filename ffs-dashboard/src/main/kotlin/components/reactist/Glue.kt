@file:Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")

package doist.ffs.components.reactist

import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import react.ReactElement
import react.dom.html.AnchorHTMLAttributes
import react.dom.html.ButtonHTMLAttributes
import react.dom.html.HTMLAttributes
import react.dom.html.InputHTMLAttributes

typealias HtmlDivProps = HTMLAttributes<HTMLDivElement>
typealias HtmlInputProps = InputHTMLAttributes<HTMLInputElement>
typealias HtmlButtonProps = ButtonHTMLAttributes<HTMLButtonElement>
typealias HtmlAnchorProps = AnchorHTMLAttributes<HTMLAnchorElement>

fun AvatarUser(): AvatarUser = js("{}") as AvatarUser

typealias ColumnsCollapseBelow = Any

typealias IconElement = ReactElement
