@file:Suppress("MatchingDeclarationName")

package doist.ffs.components

import db.Organization
import doist.ffs.KEY_ORGANIZATIONS
import doist.ffs.api
import doist.ffs.components.reactist.Button
import doist.ffs.components.reactist.Inline
import doist.ffs.components.reactist.Loading
import doist.ffs.components.reactist.Stack
import doist.ffs.components.reactist.Text
import doist.ffs.components.reactist.TextField
import doist.ffs.contexts.SessionContext
import doist.ffs.createOrganization
import doist.ffs.ext.toMutableList
import doist.ffs.listOrganizations
import doist.ffs.use
import kotlinx.browser.localStorage
import kotlinx.serialization.builtins.ListSerializer
import react.FC
import react.Props
import react.create
import react.useContext
import react.useEffectOnce
import react.useState

val Organizations = FC<Props> {
    val (_, setSession) = useContext(SessionContext)
    var organizations by localStorage.use(
        KEY_ORGANIZATIONS,
        ListSerializer(Organization.serializer())
    )
    var isLoading by useState(false)
    var name by useState("")
    var isSubmitting by useState(false)

    useEffectOnce {
        isLoading = true
        api(setSession) {
            organizations = listOrganizations()
        }.invokeOnCompletion {
            isLoading = false
        }
    }

    Stack {
        space = "large"
        dividers = "tertiary"
        paddingBottom = "xxlarge"

        organizations?.forEach { organization ->
            Text {
                size = "subtitle"
                lineClamp = 1
                +organization.name
            }

            Projects {
                organizationId = organization.id
            }
        }
    }

    if (isLoading) {
        Loading()
    }

    Inline {
        space = "small"
        alignY = "bottom"
        paddingTop = "xxlarge"

        TextField {
            type = "text"
            value = name
            placeholder = "Add organization"
            onChange = {
                name = it.target.value
            }
        }

        Button {
            variant = "primary"
            icon = SubmitIcon.create()
            disabled = name.isEmpty()
            loading = isSubmitting
            onClick = {
                isSubmitting = true
                api(setSession) {
                    val id = createOrganization(name)
                    organizations = organizations.toMutableList() + Organization(id, name)
                    name = ""
                }.invokeOnCompletion {
                    isSubmitting = false
                }
            }
        }
    }
}
