package doist.ffs.validators

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidatorsTest {
    @Test
    fun validEmails() {
        listOf(
            "test@test.test",
            "test+test@test.com",
            "test-test@test.com",
            "test-@test.com",
            "test---test@test.com",
            "t@test.test",
            "test.test.test+Test@test.test",
            "test-test@test-test.test",
            "${"a".repeat(64)}@test.test",
        ).forEach {
            assertTrue(validateEmail(it), "Expected $it to be valid.")
        }
    }

    @Test
    fun invalidEmails() {
        listOf(
            "not-an-email",
            "test@",
            "test@test.t",
            "@test.test",
            "test@127.0.0.1",
            "test+@test.test",
            "test++test@test.test",
            "test..test@test.com",
            "test/test@test.test",
            "test%test.test@test.test",
            "\" \"@test.test",
            "test@test@test.test",
            "test@test_test.test",
            "${"a".repeat(65)}@test.test",
        ).forEach {
            assertFalse(validateEmail(it), "Expected $it to be invalid.")
        }
    }

    @Test
    fun validPasswords() {
        listOf(
            "password",
            "12345678",
            "pass1234",
        ).forEach {
            assertTrue(validatePassword(it), "Expected $it to be valid.")
        }
    }

    @Test
    fun invalidPasswords() {
        listOf(
            "passwor",
            "1234567",
            "pass123",
        ).forEach {
            assertFalse(validatePassword(it), "Expected $it to be invalid.")
        }
    }
}
