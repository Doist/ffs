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
    fun insertValid(): Unit = testDatabase.flags.run {
        insert(project_id = projectId, name = NAME, rule = RULE)
    }

    @Test
    fun insertDuplicatedName(): Unit = testDatabase.flags.run {
        insert(project_id = projectId, name = NAME, rule = RULE)
        assertFails {
            insert(project_id = projectId, name = NAME, rule = RULE)
        }
    }

    @Test
    fun selectByOrganization(): Unit = testDatabase.flags.run {
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
    fun select(): Unit = testDatabase.flags.run {
        val id = testDatabase.capturingLastInsertId {
            insert(project_id = projectId, name = NAME, rule = RULE)
        }
        val flag = select(id).executeAsOne()
        assert(flag.project_id == projectId)
        assert(flag.name == NAME)
        assert(flag.rule == RULE)
    }

    @Test
    fun updateName(): Unit = testDatabase.flags.run {
        val id = testDatabase.capturingLastInsertId {
            insert(project_id = projectId, name = NAME, rule = RULE)
        }
        update(id = id, name = NAME_UPDATED, rule = RULE)
        val flag = select(id).executeAsOne()
        assert(flag.name == NAME_UPDATED)
    }

    @Test
    fun updateRule(): Unit = testDatabase.flags.run {
        val id = testDatabase.capturingLastInsertId {
            insert(project_id = projectId, name = NAME, rule = RULE)
        }
        update(id = id, name = NAME, rule = RULE_UPDATED)
        val flag = select(id).executeAsOne()
        assert(flag.rule == RULE_UPDATED)
    }

    @Test
    fun archive(): Unit = testDatabase.flags.run {
        val id = testDatabase.capturingLastInsertId {
            insert(project_id = projectId, name = NAME, rule = RULE)
        }
        var flag = select(id).executeAsOne()
        assert(flag.archived_at == null)
        archive(id)
        flag = select(id).executeAsOne()
        assert(flag.archived_at != null)
    }

    @Test
    fun unarchive(): Unit = testDatabase.flags.run {
        val id = testDatabase.capturingLastInsertId {
            insert(project_id = projectId, name = NAME, rule = RULE)
        }
        archive(id)
        unarchive(id)
        val flag = select(id).executeAsOne()
        assert(flag.archived_at == null)
    }

    companion object {
        private const val NAME = "test-project"
        private const val NAME_UPDATED = "new-test-project"
        private const val RULE = "1"
        private const val RULE_UPDATED = "0"
    }
}
