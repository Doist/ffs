package doist.ffs.db

import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import org.junit.jupiter.api.Test
import kotlin.test.assertFails

internal class UserOrganizationTest {
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
        usersOrganizations.insert(
            user_id = id,
            organization_id = organizationId,
            role = ROLE_ADMIN
        )
        val user = users.selectByOrganization(organizationId).executeAsList().single()
        assert(user.name == NAME)
        assert(user.email == EMAIL)
        assert(user.organization_id == organizationId)
        assert(user.role == ROLE_ADMIN)
    }

    @Test
    fun testInsertDuplicate(): Unit = testDatabase.run {
        val id = capturingLastInsertId {
            users.insert(name = NAME, email = EMAIL, password = PASSWORD)
        }
        usersOrganizations.insert(
            user_id = id,
            organization_id = organizationId,
            role = ROLE_MEMBER
        )
        assertFails {
            usersOrganizations.insert(
                user_id = id,
                organization_id = organizationId,
                role = ROLE_ADMIN
            )
        }
    }

    companion object {
        private const val NAME = "Gonçalo Silva"
        private const val EMAIL = "goncalo@doist.com"
        private const val PASSWORD = "password"
    }
}
