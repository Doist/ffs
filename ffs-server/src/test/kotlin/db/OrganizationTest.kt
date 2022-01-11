package doist.ffs.db

import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import kotlin.test.Test

internal class OrganizationTest {
    private val testDatabase = Database(JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY))

    @Test
    fun insertValid(): Unit = testDatabase.run {
        organizations.insert(name = ORG_NAME)
    }

    @Test
    fun select(): Unit = testDatabase.run {
        val id = capturingLastInsertId {
            organizations.insert(name = ORG_NAME)
        }
        val organization = organizations.select(id = id).executeAsOne()
        assert(organization.name == ORG_NAME)
    }

    @Test
    fun updateName(): Unit = testDatabase.run {
        val id = capturingLastInsertId {
            organizations.insert(name = ORG_NAME)
        }
        organizations.update(id = id, name = ORG_NAME_UPDATED)
        val organization = organizations.select(id = id).executeAsOne()
        assert(organization.name == ORG_NAME_UPDATED)
    }

    @Test
    fun delete(): Unit = testDatabase.run {
        val id = capturingLastInsertId {
            organizations.insert(name = ORG_NAME)
        }
        organizations.delete(id)
        val organization = organizations.select(id = id).executeAsOneOrNull()
        assert(organization == null)
    }

    companion object {
        private const val ORG_NAME = "test-organization"
        private const val ORG_NAME_UPDATED = "new-test-organization"
    }
}
