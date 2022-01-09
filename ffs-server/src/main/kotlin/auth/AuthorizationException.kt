package doist.ffs.auth

class AuthorizationException : RuntimeException()

fun authorize(condition: Boolean?) {
    if (condition == true) {
        throw AuthorizationException()
    }
}
