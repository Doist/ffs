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
        val name = "test-name"
        insert(organization_id = organizationId, name = name)
        val project = selectByOrganization(organizationId).executeAsList()[0]
        assert(project.organization_id == organizationId)
        assert(project.name == name)
    }

    @Test
    fun testInsertDuplicatedName(): Unit = testDatabase.projects.run {
        val name = "test-name"
        insert(organization_id = organizationId, name = name)
        assertFails {
            insert(organization_id = organizationId, name = name)
        }
    }

    @Test
    fun testSelectByOrganization(): Unit = testDatabase.projects.run {
        val namePrefix = "test-name-"
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
        val name = "test-name"
        val id = testDatabase.capturingLastInsertId {
            insert(organization_id = organizationId, name = name)
        }
        val project = select(id).executeAsOne()
        assert(project.organization_id == organizationId)
        assert(project.name == name)
    }

    @Test
    fun testUpdate(): Unit = testDatabase.projects.run {
        val oldName = "old-test-name"
        val newName = "new-test-name"
        val id = testDatabase.capturingLastInsertId {
            insert(organization_id = organizationId, name = oldName)
        }
        update(id = id, name = newName)
        val project = select(id).executeAsOne()
        assert(project.name == newName)
    }

    @Test
    fun testDelete(): Unit = testDatabase.projects.run {
        val name = "test-name"
        val id = testDatabase.capturingLastInsertId {
            insert(organization_id = organizationId, name = name)
        }
        delete(id)
        val project = select(id).executeAsOneOrNull()
        assert(project == null)
    }
}
