package doist.ffs

import kotlinx.serialization.KSerializer
import org.w3c.dom.Storage
import react.StateInstance
import react.useEffect
import react.useState

const val KEY_SESSION = "session"
const val KEY_USER = "user"
const val KEY_ORGANIZATIONS = "organizations"

fun <T> Storage.use(key: String, serializer: KSerializer<T>): StateInstance<T?> {
    val state = useState {
        getItem(key)?.let { json.decodeFromString(serializer, it) }
    }
    val (value, _) = state

    useEffect(key, value) {
        if (value != null) {
            setItem(key, json.encodeToString(serializer, value))
        } else {
            removeItem(key)
        }
    }

    return state
}

fun Storage.use(key: String): StateInstance<String?> {
    val state = useState {
        getItem(key)
    }
    val (value, _) = state

    useEffect(key, value) {
        if (value != null) {
            setItem(key, value)
        } else {
            removeItem(key)
        }
    }

    return state
}
