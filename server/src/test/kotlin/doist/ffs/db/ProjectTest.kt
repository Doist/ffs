package doist.ffs.db

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import doist.ffs.capturingLastInsertId
import doist.ffs.getDatabase
import doist.ffs.organizations
import doist.ffs.projects
import doist.ffs.withDatabase
import kotlin.test.Test
import kotlin.test.assertFails

internal class ProjectTest {
    private var organizationId: Long = -1
    private val testDriver: SqlDriver
        get() {
            val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
            val database = getDatabase(driver)
            organizationId = database.capturingLastInsertId {
                organizations.insert(name = "test-name")
            }
            return driver
        }

    @Test
    fun testInsertValid(): Unit = withDatabase(testDriver) { db ->
        val name = "test-name"
        db.projects.apply {
            insert(organization_id = organizationId, name = name)
            val project = db.projects.selectByOrganization(organizationId).executeAsList()[0]
            assert(project.organization_id == organizationId)
            assert(project.name == name)
        }
    }

    @Test
    fun testInsertDuplicatedName(): Unit = withDatabase(testDriver) { db ->
        val name = "test-name"
        db.projects.apply {
            insert(organization_id = organizationId, name = name)
            assertFails {
                db.projects.insert(organization_id = organizationId, name = name)
            }
        }
    }

    @Test
    fun testSelectByOrganization(): Unit = withDatabase(testDriver) { db ->
        val namePrefix = "test-name-"
        db.projects.apply {
            for (i in 0..9) {
                insert(organization_id = organizationId, name = "$namePrefix-$i")
            }
            val projects = db.projects.selectByOrganization(organizationId).executeAsList()
            assert(projects.size == 10)
            projects.forEachIndexed { index, project ->
                assert(project.name.startsWith(namePrefix))
                assert(projects.subList(index + 1, projects.size).none { project.name == it.name })
            }
        }
    }

    @Test
    fun testSelect(): Unit = withDatabase(testDriver) { db ->
        val name = "test-name"
        val id = db.capturingLastInsertId {
            projects.insert(organization_id = organizationId, name = name)
        }
        val project = db.projects.select(id).executeAsOne()
        assert(project.organization_id == organizationId)
        assert(project.name == name)
    }

    @Test
    fun testUpdate(): Unit = withDatabase(testDriver) { db ->
        val oldName = "old-test-name"
        val newName = "new-test-name"
        val id = db.capturingLastInsertId {
            projects.insert(organization_id = organizationId, name = oldName)
        }
        db.projects.update(id = id, name = newName)
        val project = db.projects.select(id).executeAsOne()
        assert(project.name == newName)
    }

    @Test
    fun testDelete(): Unit = withDatabase(testDriver) { db ->
        val name = "test-name"
        val id = db.capturingLastInsertId {
            projects.insert(organization_id = organizationId, name = name)
        }
        db.projects.delete(id)
        val project = db.projects.select(id).executeAsOneOrNull()
        assert(project == null)
    }
}
