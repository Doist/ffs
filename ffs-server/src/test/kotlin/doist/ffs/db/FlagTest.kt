package doist.ffs.db

import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import doist.ffs.ext.Database
import doist.ffs.ext.capturingLastInsertId
import doist.ffs.ext.flags
import doist.ffs.ext.organizations
import doist.ffs.ext.projects
import kotlin.test.Test
import kotlin.test.assertFails

internal class FlagTest {
    private var projectId: Long = -1
    private val testDatabase = Database(JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)).apply {
        val organizationId = capturingLastInsertId {
            organizations.insert(name = "test-name")
        }
        projectId = capturingLastInsertId {
            projects.insert(organization_id = organizationId, name = "test-name")
        }
    }

    @Test
    fun testInsertValid(): Unit = testDatabase.flags.run {
        val name = "test-name"
        val rule = "true"
        insert(project_id = projectId, name = name, rule = rule)
        val flag = selectByProject(projectId).executeAsList()[0]
        assert(flag.project_id == projectId)
        assert(flag.name == name)
    }

    @Test
    fun testInsertDuplicatedName(): Unit = testDatabase.flags.run {
        val name = "test-name"
        val rule = "true"
        insert(project_id = projectId, name = name, rule = rule)
        assertFails {
            insert(project_id = projectId, name = name, rule = rule)
        }
    }

    @Test
    fun testSelectByOrganization(): Unit = testDatabase.flags.run {
        val namePrefix = "test-name-"
        val rule = "true"
        for (i in 0..9) {
            insert(project_id = projectId, name = "$namePrefix-$i", rule = rule)
        }
        val flags = selectByProject(projectId).executeAsList()
        assert(flags.size == 10)
        flags.forEachIndexed { index, flag ->
            assert(flag.name.startsWith(namePrefix))
            assert(flags.subList(index + 1, flags.size).none { flag.name == it.name })
        }
    }

    @Test
    fun testSelect(): Unit = testDatabase.flags.run {
        val name = "test-name"
        val rule = "true"
        val id = testDatabase.capturingLastInsertId {
            insert(project_id = projectId, name = name, rule = rule)
        }
        val flag = select(id).executeAsOne()
        assert(flag.project_id == projectId)
        assert(flag.name == name)
    }

    @Test
    fun testUpdateRule(): Unit = testDatabase.flags.run {
        val name = "test-name"
        val oldRule = "true"
        val newRule = "false"
        val id = testDatabase.capturingLastInsertId {
            insert(project_id = projectId, name = name, rule = oldRule)
        }
        update(id = id, name = name, rule = newRule)
        val project = select(id).executeAsOne()
        assert(project.rule == newRule)
    }

    @Test
    fun testDelete(): Unit = testDatabase.flags.run {
        val name = "test-name"
        val rule = "true"
        val id = testDatabase.capturingLastInsertId {
            insert(project_id = projectId, name = name, rule = rule)
        }
        delete(id)
        val project = select(id).executeAsOneOrNull()
        assert(project == null)
    }
}
