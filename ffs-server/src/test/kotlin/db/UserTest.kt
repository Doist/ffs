package doist.ffs.db

import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFails

internal class UserTest {
    private var organizationId: Long = -1
    private val testDatabase = Database(JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)).apply {
        organizationId = capturingLastInsertId {
            organizations.insert(name = "test-name")
        }
    }

    @Test
    fun testInsertValid(): Unit = testDatabase.run {
        val id = capturingLastInsertId {
            users.insert(name = NAME, email = EMAIL, password = PASSWORD)
        }
        val user = users.select(id).executeAsOne()
        assert(user.name == NAME)
        assert(user.email == EMAIL)
    }

    @Test
    fun testInsertDuplicate(): Unit = testDatabase.run {
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
    fun testSelectByEmail(): Unit = testDatabase.run {
        users.insert(name = NAME, email = EMAIL, password = PASSWORD)
        val user = users.selectByEmail(EMAIL).executeAsOne()
        assert(user.name == NAME)
        assert(user.email == EMAIL)
    }

    @Test
    fun testSelectByOrganization(): Unit = testDatabase.run {
        roles.insert(
            user_id = capturingLastInsertId {
                users.insert(name = NAME, email = EMAIL, password = PASSWORD)
            },
            organization_id = organizationId,
            role = RoleEnum.ADMIN
        )
        roles.insert(
            user_id = capturingLastInsertId {
                users.insert(name = NAME_OTHER, email = EMAIL_OTHER, password = PASSWORD)
            },
            organization_id = organizationId,
            role = RoleEnum.ADMIN
        )
        val users = users.selectByOrganizationId(organizationId).executeAsList()
        assert(users.size == 2)
        assert(users.map { it.name }.containsAll(listOf(NAME, NAME_OTHER)))
        assert(users.map { it.email }.containsAll(listOf(EMAIL, EMAIL_OTHER)))
        assertContentEquals(users.map { it.role }, listOf(RoleEnum.ADMIN, RoleEnum.ADMIN))
    }

    @Test
    fun testSelectPasswordById(): Unit = testDatabase.run {
        val id = capturingLastInsertId {
            users.insert(name = NAME, email = EMAIL, password = PASSWORD)
        }
        val password = users.selectPasswordById(id).executeAsOne()
        assert(password == PASSWORD)
    }

    @Test
    fun testUpdateName(): Unit = testDatabase.users.run {
        val id = testDatabase.capturingLastInsertId {
            insert(name = NAME, email = EMAIL, password = PASSWORD)
        }
        updateName(id = id, name = NAME_OTHER)
        val user = select(id).executeAsOne()
        assert(user.name == NAME_OTHER)
    }

    @Test
    fun testUpdateEmail(): Unit = testDatabase.users.run {
        val id = testDatabase.capturingLastInsertId {
            insert(name = NAME, email = EMAIL, password = PASSWORD)
        }
        updateEmail(id = id, email = EMAIL_OTHER)
        val user = select(id).executeAsOne()
        assert(user.email == EMAIL_OTHER)
    }

    @Test
    fun testUpdatePassword(): Unit = testDatabase.users.run {
        val id = testDatabase.capturingLastInsertId {
            insert(name = NAME, email = EMAIL, password = PASSWORD)
        }
        updatePassword(id = id, password = PASSWORD_OTHER)
        assert(selectPasswordById(id).executeAsOne() == PASSWORD_OTHER)
    }

    @Test
    fun testDelete(): Unit = testDatabase.users.run {
        val id = testDatabase.capturingLastInsertId {
            insert(name = NAME, email = EMAIL, password = PASSWORD)
        }
        delete(id)
        val user = select(id).executeAsOneOrNull()
        assert(user == null)
    }

    companion object {
        private const val NAME = "Gonçalo Silva"
        private const val NAME_OTHER = "Gonçalo Santarém"
        private const val EMAIL = "goncalo@doist.com"
        private const val EMAIL_OTHER = "goncalo@doist.io"
        private const val PASSWORD = "password"
        private const val PASSWORD_OTHER = "other password"
    }
}
