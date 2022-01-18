@file:Suppress("MatchingDeclarationName")

package doist.ffs.components

import csstype.JustifyContent
import doist.ffs.KEY_PROJECTS
import doist.ffs.api
import doist.ffs.components.reactist.Button
import doist.ffs.components.reactist.ButtonLink
import doist.ffs.components.reactist.Inline
import doist.ffs.components.reactist.Loading
import doist.ffs.components.reactist.Stack
import doist.ffs.components.reactist.TextField
import doist.ffs.contexts.SessionContext
import doist.ffs.createProject
import doist.ffs.db.Project
import doist.ffs.ext.toMutableList
import doist.ffs.listProjects
import doist.ffs.use
import kotlinext.js.jso
import kotlinx.browser.localStorage
import kotlinx.serialization.builtins.ListSerializer
import react.FC
import react.Props
import react.create
import react.router.dom.Link
import react.useContext
import react.useEffectOnce
import react.useState

external interface ProjectsProps : Props {
    var organizationId: Long
}

val Projects = FC<ProjectsProps> { props ->
    val (_, setSession) = useContext(SessionContext)
    var projects by localStorage.use(
        KEY_PROJECTS(props.organizationId),
        ListSerializer(Project.serializer())
    )
    var isLoading by useState(false)
    var name by useState("")
    var isSubmitting by useState(false)

    useEffectOnce {
        isLoading = true
        api(setSession) {
            projects = listProjects(props.organizationId)
        }.invokeOnCompletion {
            isLoading = false
        }
    }

    Stack {
        space = "small"

        projects?.forEach { project ->
            ButtonLink {
                variant = "quaternary"
                style = jso {
                    justifyContent = JustifyContent.start
                }
                `as` = Link
                to = "/projects/${project.id}"
                +project.name
            }
        }
    }

    if (isLoading) {
        Loading()
    }

    Inline {
        space = "small"
        alignY = "bottom"
        paddingBottom = "medium"

        TextField {
            type = "text"
            value = name
            placeholder = "Add project"
            onChange = {
                name = it.target.value
            }
        }

        Button {
            variant = "primary"
            icon = SubmitIcon.create()
            disabled = name.isEmpty()
            loading = isSubmitting
            onClick = { event ->
                event.preventDefault()
                isSubmitting = true
                api(setSession) {
                    val id = createProject(name, props.organizationId)
                    projects = projects.toMutableList() + Project(id, name, props.organizationId)
                    name = ""
                }.invokeOnCompletion {
                    isSubmitting = false
                }
            }
        }
    }
}
