package doist.ffs.components

import doist.ffs.KEY_TOKENS
import doist.ffs.api
import doist.ffs.components.reactist.Button
import doist.ffs.components.reactist.Inline
import doist.ffs.components.reactist.Loading
import doist.ffs.components.reactist.SelectField
import doist.ffs.components.reactist.Stack
import doist.ffs.components.reactist.Text
import doist.ffs.components.reactist.TextField
import doist.ffs.contexts.SessionContext
import doist.ffs.createToken
import doist.ffs.db.Permission
import doist.ffs.db.Token
import doist.ffs.ext.toMutableList
import doist.ffs.listTokens
import doist.ffs.use
import kotlinx.browser.localStorage
import kotlinx.serialization.builtins.ListSerializer
import react.FC
import react.Props
import react.create
import react.dom.html.ReactHTML.option
import react.useContext
import react.useEffectOnce
import react.useState

external interface TokensProps : Props {
    var projectId: Long
}

val Tokens = FC<TokensProps> { props ->
    val (_, setSession) = useContext(SessionContext)
    var tokens by localStorage.use(
        KEY_TOKENS(props.projectId),
        ListSerializer(Token.serializer())
    )
    var isLoading by useState(false)
    var description by useState("")
    var permission by useState(Permission.EVAL)
    var isSubmitting by useState(false)

    useEffectOnce {
        isLoading = true
        api(setSession) {
            tokens = listTokens(props.projectId)
        }.invokeOnCompletion {
            isLoading = false
        }
    }

    Stack {
        space = "small"

        tokens?.forEach { token ->
            Stack {
                paddingTop = "xxlarge"

                Text {
                    +token.description
                }

                if (token.token != null) {
                    Text {
                        tone = "secondary"
                        +token.token
                    }
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
            value = description
            placeholder = "Token description"
            onChange = {
                description = it.target.value
            }
        }

        SelectField {
            value = permission.name
            onChange = {
                permission = Permission.valueOf(it.target.value)
            }

            option {
                value = Permission.EVAL.name
                +"Eval permissions (frontend SDK)"
            }
            option {
                value = Permission.READ.name
                +"Read permissions (backend SDK)"
            }
        }

        Button {
            variant = "primary"
            icon = SubmitIcon.create()
            disabled = description.isEmpty()
            loading = isSubmitting
            onClick = {
                isSubmitting = true
                api(setSession) {
                    val (id, token) = createToken(description, permission, props.projectId)
                    tokens = tokens.toMutableList() + Token(id, props.projectId, description, token)
                    description = ""
                    permission = Permission.EVAL
                }.invokeOnCompletion {
                    isSubmitting = false
                }
            }
        }
    }
}
