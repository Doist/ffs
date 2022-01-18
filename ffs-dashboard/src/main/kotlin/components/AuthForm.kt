package doist.ffs.components

import csstype.Margin
import doist.ffs.api
import doist.ffs.components.reactist.Button
import doist.ffs.components.reactist.PasswordField
import doist.ffs.components.reactist.Stack
import doist.ffs.components.reactist.TextField
import doist.ffs.contexts.UserContext
import doist.ffs.login
import doist.ffs.register
import doist.ffs.validators.validateEmail
import doist.ffs.validators.validatePassword
import kotlinext.js.jso
import kotlinx.browser.window
import react.FC
import react.Props
import react.dom.html.ButtonType
import react.dom.html.ReactHTML.form
import react.useContext
import react.useEffect
import react.useState

external interface AuthFormProps : Props {
    var register: Boolean
}

val AuthForm = FC<AuthFormProps> { props ->
    val (_, setUser) = useContext(UserContext)
    var name by useState("")
    var email by useState("")
    var password by useState("")
    var canSubmit by useState(false)
    var isSubmitting by useState(false)

    useEffect {
        canSubmit = validateEmail(email) && validatePassword(password)
    }

    form {
        onSubmit = { event ->
            event.preventDefault()
            isSubmitting = true
            api(setUser) {
                val user = if (props.register) {
                    register(name, email, password)
                } else {
                    login(email, password)
                }
                setUser(user)
            }.invokeOnCompletion { _ ->
                isSubmitting = false
                // TODO(goncalossilva): handle error argument
            }
        }

        Stack {
            maxWidth = "small"
            paddingTop = "xxlarge"
            paddingBottom = "xxlarge"
            space = "large"
            style = jso {
                margin = Margin("auto")
            }

            window.localStorage

            if (props.register) {
                TextField {
                    label = "Name"
                    type = "text"
                    autoFocus = true
                    onChange = {
                        name = it.target.value
                    }
                }
            }

            TextField {
                label = "Email"
                type = "email"
                autoFocus = !props.register
                onChange = {
                    email = it.target.value
                }
            }

            PasswordField {
                label = "Password"
                togglePasswordLabel = "Toggle password visibility"
                onChange = {
                    password = it.target.value
                }
            }

            Button {
                variant = "primary"
                type = ButtonType.submit
                disabled = !canSubmit
                loading = isSubmitting
                +"Submit"
            }
        }
    }
}
