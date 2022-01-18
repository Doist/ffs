@file:Suppress("MatchingDeclarationName", "MaxLineLength")

package doist.ffs.components

import react.FC
import react.Props
import react.dom.svg.ReactSVG.path
import react.dom.svg.ReactSVG.svg

val SubmitIcon = FC<Props> {
    @Suppress("MagicNumber", "MaxLineLength")
    svg {
        width = 24.0
        height = 24.0
        viewBox = "0 0 24 24"
        path {
            fill = "currentColor"
            d = "M6 6.653a1 1 0 011.464-.886l10.246 5.37a1 1 0 01-.002 1.773L7.46 18.24a1 1 0 01-1.461-.887V13l6.96-.674a.328.328 0 000-.652L6 11V6.653z"
        }
    }
}
