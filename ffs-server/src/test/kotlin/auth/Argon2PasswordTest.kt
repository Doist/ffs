package auth

import doist.ffs.auth.Argon2Password
import kotlin.test.Test

class Argon2PasswordTest {
    @Test
    fun testEncodeDoesNotEqualRaw() {
        assert(Argon2Password.encode("password") != "password")
    }

    @Test
    fun testEncodeIsEqualWhenPasswordIsEqual() {
        assert(Argon2Password.matches("password", Argon2Password.encode("password")))
    }

    @Test
    fun testEncodeIsEqualWhenPasswordIsEqualUnicode() {
        val result = Argon2Password.encode("passw\u9292rd")
        assert(!Argon2Password.matches("pass\u9292\u9292rd", result))
        assert(Argon2Password.matches("passw\u9292rd", result))
    }

    @Test
    fun testEncodeIsDifferentWhenPasswordIsDifferent() {
        assert(!Argon2Password.matches("password", Argon2Password.encode("pwd")))
    }

    @Test
    fun testEncodeIsDifferentWhenEncodedIsEmpty() {
        assert(!Argon2Password.matches("password", ""))
    }

    @Test
    fun testEncodeTwiceIsDifferent() {
        assert(Argon2Password.encode("password") != Argon2Password.encode("password"))
    }
}
