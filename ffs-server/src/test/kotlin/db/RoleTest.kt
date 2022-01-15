package doist.ffs.db

import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import org.junit.jupiter.api.Test
import kotlin.test.assertFails

internal class RoleTest {
    private var userId: Long = -1
    private var organizationId: Long = -1
    private val testDatabase = Database(JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)).apply {
        userId = capturingLastInsertId {
            users.insert(name = USER_NAME, email = USER_EMAIL, password = USER_PASSWORD)
        }
        organizationId = capturingLastInsertId {
            organizations.insert(name = ORG_NAME)
        }
    }

    @Test
    fun insertValid(): Unit = testDatabase.run {
        roles.insert(user_id = userId, organization_id = organizationId, role = RoleEnum.USER)
    }

    @Test
    fun insertDuplicate(): Unit = testDatabase.run {
        roles.insert(user_id = userId, organization_id = organizationId, role = RoleEnum.USER)
        assertFails {
            roles.insert(user_id = userId, organization_id = organizationId, role = RoleEnum.ADMIN)
        }
    }

    @Test
    fun selectOrganizationByUser(): Unit = testDatabase.run {
        roles.insert(user_id = userId, organization_id = organizationId, role = RoleEnum.USER)
        val organizations = roles.selectOrganizationByUser(user_id = userId).executeAsList()
        assert(organizations.size == 1)
        assert(organizations[0].name == ORG_NAME)
        assert(organizations[0].role == RoleEnum.USER)
    }

    @Test
    fun selectOrganizationIdProjectIdByUser(): Unit = testDatabase.run {
        roles.insert(user_id = userId, organization_id = organizationId, role = RoleEnum.READER)
        val projectId = capturingLastInsertId {
            projects.insert(organization_id = organizationId, name = "ffs")
        }
        val organizations = roles.selectOrganizationIdProjectIdByUser(
            user_id = userId
        ).executeAsList()
        assert(organizations.size == 1)
        assert(organizations[0].id == organizationId)
        assert(organizations[0].project_id == projectId)
        assert(organizations[0].role == RoleEnum.READER)
    }

    @Test
    fun update(): Unit = testDatabase.run {
        roles.insert(user_id = userId, organization_id = organizationId, role = RoleEnum.USER)
        roles.update(user_id = userId, organization_id = organizationId, role = RoleEnum.ADMIN)
        val organization = roles.selectOrganizationByUser(user_id = userId).executeAsOne()
        assert(organization.role == RoleEnum.ADMIN)
    }

    @Test
    fun delete(): Unit = testDatabase.run {
        roles.insert(user_id = userId, organization_id = organizationId, role = RoleEnum.READER)
        roles.delete(user_id = userId, organization_id = organizationId)
        val organization = roles.selectOrganizationByUser(user_id = userId).executeAsOneOrNull()
        assert(organization == null)
    }

    companion object {
        private const val USER_NAME = "Test User"
        private const val USER_EMAIL = "test@test.test"
        private const val USER_PASSWORD = "password"

        private const val ORG_NAME = "Doist"
    }
}
