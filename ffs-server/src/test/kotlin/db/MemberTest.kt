package doist.ffs.db

import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import org.junit.Test
import kotlin.test.assertFails

internal class MemberTest {
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
        members.insert(user_id = userId, organization_id = organizationId, role = Role.USER)
    }

    @Test
    fun insertDuplicate(): Unit = testDatabase.run {
        members.insert(user_id = userId, organization_id = organizationId, role = Role.USER)
        assertFails {
            members.insert(
                user_id = userId,
                organization_id = organizationId,
                role = Role.ADMIN
            )
        }
    }

    @Test
    fun selectOrganizationByUser(): Unit = testDatabase.run {
        members.insert(user_id = userId, organization_id = organizationId, role = Role.USER)
        val organizations = members.selectOrganizationByUserId(user_id = userId).executeAsList()
        assert(organizations.size == 1)
        assert(organizations[0].name == ORG_NAME)
        assert(organizations[0].role == Role.USER)
    }

    @Test
    fun selectOrganizationIdProjectIdByUser(): Unit = testDatabase.run {
        members.insert(user_id = userId, organization_id = organizationId, role = Role.READER)
        val projectId = capturingLastInsertId {
            projects.insert(organization_id = organizationId, name = "ffs")
        }
        val organizations = members.selectOrganizationIdProjectIdByUserId(
            user_id = userId
        ).executeAsList()
        assert(organizations.size == 1)
        assert(organizations[0].id == organizationId)
        assert(organizations[0].project_id == projectId)
        assert(organizations[0].role == Role.READER)
    }

    @Test
    fun update(): Unit = testDatabase.run {
        members.insert(user_id = userId, organization_id = organizationId, role = Role.USER)
        members.update(user_id = userId, organization_id = organizationId, role = Role.ADMIN)
        val organization = members.selectOrganizationByUserId(user_id = userId).executeAsOne()
        assert(organization.role == Role.ADMIN)
    }

    @Test
    fun delete(): Unit = testDatabase.run {
        members.insert(user_id = userId, organization_id = organizationId, role = Role.READER)
        members.delete(user_id = userId, organization_id = organizationId)
        val organization = members.selectOrganizationByUserId(user_id = userId).executeAsOneOrNull()
        assert(organization == null)
    }

    companion object {
        private const val USER_NAME = "Test User"
        private const val USER_EMAIL = "test@test.test"
        private const val USER_PASSWORD = "password"

        private const val ORG_NAME = "Doist"
    }
}
