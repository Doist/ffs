package doist.ffs.validators

/**
 * Validates the email.
 */
private val EMAIL_REGEXP = Regex(
    "^(?=.{1,64}@)[\\p{L}0-9_-]+([\\.+][\\p{L}0-9_-]+)*" +
        "@[^-][\\p{L}0-9-]+(\\.[\\p{L}0-9-]+)*" +
        "(\\.[\\p{L}]{2,})\$"
)
fun validateEmail(email: String) = email.matches(EMAIL_REGEXP)

/**
 * Validates the password.
 */
private val PASSWORD_REGEXP = Regex(".{8,}")
fun validatePassword(password: String) = password.matches(PASSWORD_REGEXP)
