package doist.ffs.components

import doist.ffs.KEY_FLAGS
import doist.ffs.api
import doist.ffs.components.reactist.Button
import doist.ffs.components.reactist.Inline
import doist.ffs.components.reactist.Loading
import doist.ffs.components.reactist.Stack
import doist.ffs.components.reactist.Text
import doist.ffs.components.reactist.TextField
import doist.ffs.contexts.SessionContext
import doist.ffs.createFlag
import doist.ffs.db.Flag
import doist.ffs.ext.toMutableList
import doist.ffs.listFlags
import doist.ffs.rule.validateFormula
import doist.ffs.use
import kotlinx.browser.localStorage
import kotlinx.serialization.builtins.ListSerializer
import react.FC
import react.Props
import react.create
import react.useContext
import react.useEffectOnce
import react.useState

external interface FlagsProps : Props {
    var projectId: Long
}

val Flags = FC<FlagsProps> { props ->
    val (_, setSession) = useContext(SessionContext)
    var flags by localStorage.use(
        KEY_FLAGS(props.projectId),
        ListSerializer(Flag.serializer())
    )
    var isLoading by useState(false)
    var name by useState("")
    var rule by useState("")
    var isSubmitting by useState(false)

    useEffectOnce {
        isLoading = true
        api(setSession) {
            flags = listFlags(props.projectId)
        }.invokeOnCompletion {
            isLoading = false
        }
    }

    Stack {
        space = "small"

        flags?.filter { it.archivedAt == null }?.forEach { flag ->
            Stack {
                paddingTop = "xxlarge"
                space = "small"

                Text {
                    +flag.name
                }

                Text {
                    tone = "secondary"
                    +flag.rule
                }
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
            placeholder = "Flag name"
            onChange = {
                name = it.target.value
            }
        }

        TextField {
            type = "text"
            value = rule
            placeholder = "Flag rule"
            onChange = {
                rule = it.target.value
            }
        }

        Button {
            variant = "primary"
            icon = SubmitIcon.create()
            disabled = name.isEmpty() ||
                flags.orEmpty().any { name == it.name } ||
                !validateFormula(rule)
            loading = isSubmitting
            onClick = {
                isSubmitting = true
                api(setSession) {
                    val id = createFlag(name, rule, props.projectId)
                    flags = flags.toMutableList() + Flag(id, props.projectId, name, rule, null)
                    name = ""
                    rule = ""
                }.invokeOnCompletion {
                    isSubmitting = false
                }
            }
        }
    }
}
