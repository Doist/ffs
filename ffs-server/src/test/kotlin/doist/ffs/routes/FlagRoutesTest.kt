package doist.ffs.routes

import doist.ffs.FlagSerializer
import doist.ffs.ext.capturingLastInsertId
import doist.ffs.ext.database
import doist.ffs.ext.flags
import doist.ffs.ext.organizations
import doist.ffs.ext.projects
import kotlin.test.Test

class FlagRoutesTest {
    @Test
    fun testFlagCreate() = withTestApplication {
        val projectId = createProject()
        assertResourceCreates(
            PATH_FLAGS,
            listOf("project_id" to projectId.toString(), "name" to NAME, "rule" to RULE)
        )
        val projects = database.flags.selectByProject(projectId).executeAsList()
        assert(projects.size == 1)
        assert(projects[0].name == NAME)
        assert(projects[0].rule == RULE)
    }

    @Test
    fun testFlagCreateLocation() = withTestApplication {
        val projectId = createProject()
        val location = assertResourceCreates(
            PATH_FLAGS,
            listOf("project_id" to projectId.toString(), "name" to NAME, "rule" to RULE)
        )
        assertResource(location, FlagSerializer)
    }

    @Test
    fun testFlagRead() = withTestApplication {
        val projectId = createProject()
        val pathFlagsForProject = "$PATH_FLAGS?project_id=$projectId"
        assertResourceCount(pathFlagsForProject, FlagSerializer, 0)
        val id = database.capturingLastInsertId {
            flags.insert(project_id = projectId, name = NAME, rule = RULE)
        }
        assertResourceCount(pathFlagsForProject, FlagSerializer, 1)
        assertResource(PATH_FLAG(id), FlagSerializer) { flag ->
            assert(flag.id == id)
            assert(flag.project_id == projectId)
            assert(flag.name == NAME)
            assert(flag.rule == RULE)
        }
        database.flags.delete(id)
        assertResourceCount(pathFlagsForProject, FlagSerializer, 0)
    }

    @Test
    fun testFlagUpdate() = withTestApplication {
        val projectId = createProject()
        val id = database.capturingLastInsertId {
            flags.insert(project_id = projectId, name = NAME, rule = RULE)
        }
        assertResourceUpdates(PATH_FLAG(id), listOf("name" to NAME_UPDATED))
        var project = database.flags.select(id).executeAsOne()
        assert(project.id == id)
        assert(project.project_id == projectId)
        assert(project.name == NAME_UPDATED)
        assert(project.rule == RULE)
        assertResourceUpdates(PATH_FLAG(id), listOf("rule" to RULE_UPDATED))
        project = database.flags.select(id).executeAsOne()
        assert(project.id == id)
        assert(project.project_id == projectId)
        assert(project.name == NAME_UPDATED)
        assert(project.rule == RULE_UPDATED)
        assertResourceUpdates(PATH_FLAG(id), listOf("name" to NAME, "rule" to RULE))
        project = database.flags.select(id).executeAsOne()
        assert(project.id == id)
        assert(project.project_id == projectId)
        assert(project.name == NAME)
        assert(project.rule == RULE)
    }

    @Test
    fun testFlagDelete() = withTestApplication {
        val projectId = createProject()
        val id = database.capturingLastInsertId {
            flags.insert(project_id = projectId, name = NAME, rule = RULE)
        }
        assertResourceDeletes(PATH_FLAG(id))
        val flag = database.flags.select(id).executeAsOneOrNull()
        assert(flag == null)
    }

    private fun createProject(): Long {
        val organizationId = database.capturingLastInsertId {
            organizations.insert(name = "test-organization")
        }
        return database.capturingLastInsertId {
            projects.insert(organization_id = organizationId, name = "test-project")
        }
    }

    companion object {
        private const val NAME = "old-test-project"
        private const val NAME_UPDATED = "new-test-project"
        private const val RULE = "1"
        private const val RULE_UPDATED = "0"
    }
}
