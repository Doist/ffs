package doist.ffs.db

import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
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
        insert(project_id = projectId, name = NAME, rule = RULE)
        val flag = selectByProject(projectId).executeAsList()[0]
        assert(flag.project_id == projectId)
        assert(flag.name == NAME)
        assert(flag.rule == RULE)
    }

    @Test
    fun testInsertDuplicatedName(): Unit = testDatabase.flags.run {
        insert(project_id = projectId, name = NAME, rule = RULE)
        assertFails {
            insert(project_id = projectId, name = NAME, rule = RULE)
        }
    }

    @Test
    fun testSelectByOrganization(): Unit = testDatabase.flags.run {
        val namePrefix = "$NAME-"
        for (i in 0..9) {
            insert(project_id = projectId, name = "$namePrefix-$i", rule = RULE)
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
        val id = testDatabase.capturingLastInsertId {
            insert(project_id = projectId, name = NAME, rule = RULE)
        }
        val flag = select(id).executeAsOne()
        assert(flag.project_id == projectId)
        assert(flag.name == NAME)
        assert(flag.rule == RULE)
    }

    @Test
    fun testUpdateName(): Unit = testDatabase.flags.run {
        val id = testDatabase.capturingLastInsertId {
            insert(project_id = projectId, name = NAME, rule = RULE)
        }
        update(id = id, name = NAME_UPDATED, rule = RULE)
        val project = select(id).executeAsOne()
        assert(project.name == NAME_UPDATED)
    }

    @Test
    fun testUpdateRule(): Unit = testDatabase.flags.run {
        val id = testDatabase.capturingLastInsertId {
            insert(project_id = projectId, name = NAME, rule = RULE)
        }
        update(id = id, name = NAME, rule = RULE_UPDATED)
        val project = select(id).executeAsOne()
        assert(project.rule == RULE_UPDATED)
    }

    @Test
    fun testDelete(): Unit = testDatabase.flags.run {
        val id = testDatabase.capturingLastInsertId {
            insert(project_id = projectId, name = NAME, rule = RULE)
        }
        delete(id)
        val project = select(id).executeAsOneOrNull()
        assert(project == null)
    }

    companion object {
        private const val NAME = "old-test-project"
        private const val NAME_UPDATED = "new-test-project"
        private const val RULE = "1"
        private const val RULE_UPDATED = "0"
    }
}
