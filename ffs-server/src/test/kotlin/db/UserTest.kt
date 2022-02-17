package doist.ffs.db

import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import org.junit.Test
import kotlin.test.assertFails

internal class UserTest {
    private var organizationId: Long = -1
    private val testDatabase = Database(JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)).apply {
        organizationId = capturingLastInsertId {
            organizations.insert(name = "test-name")
        }
    }

    @Test
    fun insertValid(): Unit = testDatabase.run {
        val id = capturingLastInsertId {
            users.insert(name = NAME, email = EMAIL, password = PASSWORD)
        }
        val user = users.select(id).executeAsOne()
        assert(user.name == NAME)
        assert(user.email == EMAIL)
    }

    @Test
    fun insertDuplicate(): Unit = testDatabase.run {
        users.insert(name = NAME, email = EMAIL, password = PASSWORD)
        assertFails {
            users.insert(name = NAME_OTHER, email = EMAIL, password = PASSWORD)
        }
        users.insert(name = NAME, email = EMAIL_OTHER, password = PASSWORD)
        assertFails {
            users.insert(name = NAME_OTHER, email = EMAIL_OTHER, password = PASSWORD)
        }
    }

    @Test
    fun selectByEmail(): Unit = testDatabase.run {
        users.insert(name = NAME, email = EMAIL, password = PASSWORD)
        val user = users.selectByEmail(EMAIL).executeAsOne()
        assert(user.name == NAME)
        assert(user.email == EMAIL)
    }

    @Test
    fun selectPasswordById(): Unit = testDatabase.run {
        val id = capturingLastInsertId {
            users.insert(name = NAME, email = EMAIL, password = PASSWORD)
        }
        val password = users.selectPasswordById(id).executeAsOne()
        assert(password == PASSWORD)
    }

    @Test
    fun updateName(): Unit = testDatabase.users.run {
        val id = testDatabase.capturingLastInsertId {
            insert(name = NAME, email = EMAIL, password = PASSWORD)
        }
        updateName(id = id, name = NAME_OTHER)
        val user = select(id).executeAsOne()
        assert(user.name == NAME_OTHER)
    }

    @Test
    fun updateEmail(): Unit = testDatabase.users.run {
        val id = testDatabase.capturingLastInsertId {
            insert(name = NAME, email = EMAIL, password = PASSWORD)
        }
        updateEmail(id = id, email = EMAIL_OTHER)
        val user = select(id).executeAsOne()
        assert(user.email == EMAIL_OTHER)
    }

    @Test
    fun updatePassword(): Unit = testDatabase.users.run {
        val id = testDatabase.capturingLastInsertId {
            insert(name = NAME, email = EMAIL, password = PASSWORD)
        }
        updatePassword(id = id, password = PASSWORD_OTHER)
        assert(selectPasswordById(id).executeAsOne() == PASSWORD_OTHER)
    }

    @Test
    fun delete(): Unit = testDatabase.users.run {
        val id = testDatabase.capturingLastInsertId {
            insert(name = NAME, email = EMAIL, password = PASSWORD)
        }
        delete(id)
        val user = select(id).executeAsOneOrNull()
        assert(user == null)
    }

    companion object {
        private const val NAME = "Test User"
        private const val NAME_OTHER = "User Test"
        private const val EMAIL = "test@test.test"
        private const val EMAIL_OTHER = "test@test.com"
        private const val PASSWORD = "password123"
        private const val PASSWORD_OTHER = "123password"
    }
}
