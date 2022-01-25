package doist.ffs.components

import csstype.Margin
import doist.ffs.KEY_USER
import doist.ffs.api
import doist.ffs.components.reactist.Button
import doist.ffs.components.reactist.PasswordField
import doist.ffs.components.reactist.Stack
import doist.ffs.components.reactist.TextField
import doist.ffs.contexts.SessionContext
import doist.ffs.db.User
import doist.ffs.login
import doist.ffs.register
import doist.ffs.use
import doist.ffs.validators.validateEmail
import doist.ffs.validators.validatePassword
import kotlinext.js.jso
import kotlinx.browser.localStorage
import react.FC
import react.Props
import react.dom.html.ButtonType
import react.dom.html.ReactHTML.form
import react.router.useNavigate
import react.useContext
import react.useEffect
import react.useState

external interface AuthFormProps : Props {
    var register: Boolean?
}

val AuthForm = FC<AuthFormProps> { props ->
    val (_, setSession) = useContext(SessionContext)
    val (_, setUser) = localStorage.use(KEY_USER, User.serializer())
    val navigate = useNavigate()
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
            api(setSession) {
                setUser(
                    if (props.register == true) {
                        register(name, email, password)
                    } else {
                        login(email, password)
                    }
                )
                isSubmitting = false
                navigate(to = "/app")
            }.invokeOnCompletion { error ->
                if (error != null) {
                    isSubmitting = false
                }
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

            if (props.register == true) {
                TextField {
                    label = "Name"
                    type = "text"
                    value = name
                    autoFocus = true
                    onChange = {
                        name = it.target.value
                    }
                }
            }

            TextField {
                label = "Email"
                type = "email"
                value = email
                autoFocus = props.register != true
                onChange = {
                    email = it.target.value
                }
            }

            PasswordField {
                label = "Password"
                value = password
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
