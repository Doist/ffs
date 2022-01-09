package doist.ffs.db

import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import kotlin.test.Test
import kotlin.test.assertFails

internal class ProjectTest {
    private var organizationId: Long = -1
    private val testDatabase = Database(JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)).apply {
        organizationId = capturingLastInsertId {
            organizations.insert(name = "test-name")
        }
    }

    @Test
    fun testInsertValid(): Unit = testDatabase.run {
        projects.insert(organization_id = organizationId, name = NAME)
    }

    @Test
    fun testInsertDuplicatedName(): Unit = testDatabase.run {
        projects.insert(organization_id = organizationId, name = NAME)
        assertFails {
            projects.insert(organization_id = organizationId, name = NAME)
        }
    }

    @Test
    fun testSelect(): Unit = testDatabase.run {
        val id = capturingLastInsertId {
            projects.insert(organization_id = organizationId, name = NAME)
        }
        val project = projects.select(id).executeAsOne()
        assert(project.organization_id == organizationId)
        assert(project.name == NAME)
    }

    @Test
    fun testSelectByOrganization(): Unit = testDatabase.run {
        val namePrefix = "$NAME-"
        for (i in 0..9) {
            projects.insert(organization_id = organizationId, name = "$namePrefix-$i")
        }
        val projects = projects.selectByOrganization(organizationId).executeAsList()
        assert(projects.size == 10)
        projects.forEachIndexed { i, project ->
            assert(project.name.startsWith(namePrefix))
            assert(projects.subList(i + 1, projects.size).none { project.name == it.name })
        }
    }

    @Test
    fun testUpdate(): Unit = testDatabase.run {
        val id = capturingLastInsertId {
            projects.insert(organization_id = organizationId, name = NAME)
        }
        projects.update(id = id, name = NAME_UPDATED)
        val project = projects.select(id).executeAsOne()
        assert(project.name == NAME_UPDATED)
    }

    @Test
    fun testDelete(): Unit = testDatabase.run {
        val id = capturingLastInsertId {
            projects.insert(organization_id = organizationId, name = NAME)
        }
        projects.delete(id)
        val project = projects.select(id).executeAsOneOrNull()
        assert(project == null)
    }

    companion object {
        private const val NAME = "test-project"
        private const val NAME_UPDATED = "new-test-project"
    }
}
