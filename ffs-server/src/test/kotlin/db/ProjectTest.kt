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
    fun testInsertValid(): Unit = testDatabase.projects.run {
        insert(organization_id = organizationId, name = NAME)
        val project = selectByOrganization(organizationId).executeAsList()[0]
        assert(project.organization_id == organizationId)
        assert(project.name == NAME)
    }

    @Test
    fun testInsertDuplicatedName(): Unit = testDatabase.projects.run {
        insert(organization_id = organizationId, name = NAME)
        assertFails {
            insert(organization_id = organizationId, name = NAME)
        }
    }

    @Test
    fun testSelectByOrganization(): Unit = testDatabase.projects.run {
        val namePrefix = "$NAME-"
        for (i in 0..9) {
            insert(organization_id = organizationId, name = "$namePrefix-$i")
        }
        val projects = selectByOrganization(organizationId).executeAsList()
        assert(projects.size == 10)
        projects.forEachIndexed { index, project ->
            assert(project.name.startsWith(namePrefix))
            assert(projects.subList(index + 1, projects.size).none { project.name == it.name })
        }
    }

    @Test
    fun testSelect(): Unit = testDatabase.projects.run {
        val id = testDatabase.capturingLastInsertId {
            insert(organization_id = organizationId, name = NAME)
        }
        val project = select(id).executeAsOne()
        assert(project.organization_id == organizationId)
        assert(project.name == NAME)
    }

    @Test
    fun testUpdate(): Unit = testDatabase.projects.run {
        val id = testDatabase.capturingLastInsertId {
            insert(organization_id = organizationId, name = NAME)
        }
        update(id = id, name = NAME_UPDATED)
        val project = select(id).executeAsOne()
        assert(project.name == NAME_UPDATED)
    }

    @Test
    fun testDelete(): Unit = testDatabase.projects.run {
        val id = testDatabase.capturingLastInsertId {
            insert(organization_id = organizationId, name = NAME)
        }
        delete(id)
        val project = select(id).executeAsOneOrNull()
        assert(project == null)
    }

    companion object {
        private const val NAME = "test-project"
        private const val NAME_UPDATED = "new-test-project"
    }
}
