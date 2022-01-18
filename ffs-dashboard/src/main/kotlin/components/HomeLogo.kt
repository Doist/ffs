@file:Suppress("MagicNumber")

package doist.ffs.components

import react.FC
import react.Props
import react.dom.html.ReactHTML.img
import react.router.dom.Link

val HomeLogo = FC<Props> {
    Link {
        to = "/"

        img {
            src = "icon-192.png"
            width = 32.0
            height = 32.0
            alt = "FFS logo"
        }
    }
}
