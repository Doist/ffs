package doist.ffs.db

import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import kotlin.test.Test
import kotlin.test.assertFails

internal class OrganizationTest {
    private val testDatabase = Database(JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY))

    @Test
    fun testInsertValid(): Unit = testDatabase.organizations.run {
        insert(name = NAME)
        val organization = selectAll().executeAsList()[0]
        assert(organization.name == NAME)
    }

    @Test
    fun testInsertDuplicatedName(): Unit = testDatabase.organizations.run {
        insert(name = NAME)
        assertFails {
            insert(name = NAME)
        }
    }

    @Test
    fun testSelectAll(): Unit = testDatabase.organizations.run {
        val namePrefix = "$NAME-"
        for (i in 0..9) {
            insert(name = "$namePrefix-$i")
        }
        val organizations = selectAll().executeAsList()
        assert(organizations.size == 10)
        organizations.forEachIndexed { index, organization ->
            assert(organization.name.startsWith(namePrefix))
            assert(
                organizations.subList(index + 1, organizations.size).none {
                    organization.name == it.name
                }
            )
        }
    }

    @Test
    fun testSelect(): Unit = testDatabase.organizations.run {
        val id = testDatabase.capturingLastInsertId {
            insert(name = NAME)
        }
        val organization = select(id).executeAsOne()
        assert(organization.name == NAME)
    }

    @Test
    fun testUpdateName(): Unit = testDatabase.organizations.run {
        val id = testDatabase.capturingLastInsertId {
            insert(name = NAME)
        }
        update(id = id, name = NAME_UPDATED)
        val organization = select(id).executeAsOne()
        assert(organization.name == NAME_UPDATED)
    }

    @Test
    fun testDelete(): Unit = testDatabase.organizations.run {
        val id = testDatabase.capturingLastInsertId {
            insert(name = NAME)
        }
        delete(id)
        val organization = select(id).executeAsOneOrNull()
        assert(organization == null)
    }

    companion object {
        private const val NAME = "test-organization"
        private const val NAME_UPDATED = "new-test-organization"
    }
}
