package doist.ffs.data

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import doist.ffs.capturingLastInsertId
import doist.ffs.withDatabase
import kotlin.test.Test
import kotlin.test.assertFails

internal class OrganizationTest {
    private val testDriver: SqlDriver
        get() = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)

    @Test
    fun testInsertValid(): Unit = withDatabase(testDriver) { db ->
        val name = "test-organization"
        db.organizationQueries.insert(name = name)
        val organization = db.organizationQueries.selectAll().executeAsList()[0]
        assert(organization.name == name)
    }

    @Test
    fun testInsertDuplicatedName(): Unit = withDatabase(testDriver) { db ->
        val name = "test-organization"
        db.organizationQueries.insert(name = name)
        assertFails {
            db.organizationQueries.insert(name = name)
        }
    }

    @Test
    fun testSelectAll(): Unit = withDatabase(testDriver) { db ->
        val namePrefix = "test-organization-"
        for (i in 0..9) {
            db.organizationQueries.insert(name = "$namePrefix-$i")
        }
        val organizations = db.organizationQueries.selectAll().executeAsList()
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
    fun testSelect(): Unit = withDatabase(testDriver) { db ->
        val name = "test-organization"
        val id = db.capturingLastInsertId {
            organizationQueries.insert(name = name)
        }
        val organization = db.organizationQueries.select(id).executeAsOne()
        assert(organization.name == name)
    }

    @Test
    fun testUpdateName(): Unit = withDatabase(testDriver) { db ->
        val oldName = "old-test-organization"
        val newName = "new-test-organization"
        val id = db.capturingLastInsertId {
            organizationQueries.insert(name = oldName)
        }
        db.organizationQueries.update(id = id, name = newName)
        val organization = db.organizationQueries.select(id).executeAsOne()
        assert(organization.name == newName)
    }

    @Test
    fun testDelete(): Unit = withDatabase(testDriver) { db ->
        val name = "test-organization"
        val id = db.capturingLastInsertId {
            organizationQueries.insert(name = name)
        }
        db.organizationQueries.delete(id)
        val organization = db.organizationQueries.select(id).executeAsOneOrNull()
        assert(organization == null)
    }
}
