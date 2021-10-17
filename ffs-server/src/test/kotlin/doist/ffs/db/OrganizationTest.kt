package doist.ffs.db

import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import doist.ffs.ext.Database
import doist.ffs.ext.capturingLastInsertId
import doist.ffs.ext.organizations
import kotlin.test.Test
import kotlin.test.assertFails

internal class OrganizationTest {
    private val testDatabase = Database(JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY))

    @Test
    fun testInsertValid(): Unit = testDatabase.organizations.run {
        val name = "test-organization"
        insert(name = name)
        val organization = selectAll().executeAsList()[0]
        assert(organization.name == name)
    }

    @Test
    fun testInsertDuplicatedName(): Unit = testDatabase.organizations.run {
        val name = "test-organization"
        insert(name = name)
        assertFails {
            insert(name = name)
        }
    }

    @Test
    fun testSelectAll(): Unit = testDatabase.organizations.run {
        val namePrefix = "test-organization-"
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
        val name = "test-organization"
        val id = testDatabase.capturingLastInsertId {
            insert(name = name)
        }
        val organization = select(id).executeAsOne()
        assert(organization.name == name)
    }

    @Test
    fun testUpdateName(): Unit = testDatabase.organizations.run {
        val oldName = "old-test-organization"
        val newName = "new-test-organization"
        val id = testDatabase.capturingLastInsertId {
            insert(name = oldName)
        }
        update(id = id, name = newName)
        val organization = select(id).executeAsOne()
        assert(organization.name == newName)
    }

    @Test
    fun testDelete(): Unit = testDatabase.organizations.run {
        val name = "test-organization"
        val id = testDatabase.capturingLastInsertId {
            insert(name = name)
        }
        delete(id)
        val organization = select(id).executeAsOneOrNull()
        assert(organization == null)
    }
}
