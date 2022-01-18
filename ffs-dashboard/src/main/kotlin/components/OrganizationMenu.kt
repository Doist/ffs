package doist.ffs.components

import doist.ffs.models.Organization
import react.FC
import react.Props
import react.dom.html.ReactHTML

external interface OrganizationMenuProps : Props {
    var organizations: List<Organization>
}

val OrganizationMenu = FC<OrganizationMenuProps> { props ->
    for (organization in props.organizations) {
        ReactHTML.div {
            +organization.name
        }
    }
}
