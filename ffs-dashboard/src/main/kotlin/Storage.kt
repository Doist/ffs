package doist.ffs

import kotlinx.serialization.KSerializer
import org.w3c.dom.Storage
import react.StateInstance
import react.useEffect
import react.useState

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
