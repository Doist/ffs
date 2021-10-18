package doist.ffs.routes

import doist.ffs.db.Flag
import doist.ffs.db.capturingLastInsertId
import doist.ffs.db.flags
import doist.ffs.db.organizations
import doist.ffs.db.projects
import doist.ffs.ext.handleSse
import doist.ffs.module
import doist.ffs.plugins.database
import doist.ffs.serialization.json
import io.ktor.application.Application
import io.ktor.server.testing.withTestApplication
import io.ktor.utils.io.readUTF8Line
import kotlinx.serialization.decodeFromString
import kotlin.test.Test

class FlagRoutesTest {
    @Test
    fun testFlagCreate() = withTestApplication(Application::module) {
        val projectId = createProject(application)
        assertResourceCreates(
            PATH_FLAGS,
            listOf("project_id" to projectId.toString(), "name" to NAME, "rule" to RULE)
        )
        val projects = application.database.flags.selectByProject(projectId).executeAsList()
        assert(projects.size == 1)
        assert(projects[0].name == NAME)
        assert(projects[0].rule == RULE)
    }

    @Test
    fun testFlagCreateLocation() = withTestApplication(Application::module) {
        val projectId = createProject(application)
        val location = assertResourceCreates(
            PATH_FLAGS,
            listOf("project_id" to projectId.toString(), "name" to NAME, "rule" to RULE)
        )
        assertResource<Flag>(location)
    }

    @Test
    fun testFlagRead() = withTestApplication(Application::module) {
        val projectId = createProject(application)
        val pathFlagsForProject = "$PATH_FLAGS?project_id=$projectId"
        assertResourceCount<Flag>(pathFlagsForProject, 0)
        val id = application.database.capturingLastInsertId {
            flags.insert(project_id = projectId, name = NAME, rule = RULE)
        }
        assertResourceCount<Flag>(pathFlagsForProject, 1)
        assertResource<Flag>(PATH_FLAG(id)) { flag ->
            assert(flag.id == id)
            assert(flag.project_id == projectId)
            assert(flag.name == NAME)
            assert(flag.rule == RULE)
        }
        application.database.flags.delete(id)
        assertResourceCount<Flag>(pathFlagsForProject, 0)
    }

    @Test
    fun testFlagStream(): Unit = withTestApplication(Application::module) {
        val projectId = createProject(application)
        val pathFlagsForProject = "$PATH_FLAGS?project_id=$projectId"
        handleSse(pathFlagsForProject) { channel ->
            val readLine = suspend { channel.readUTF8Line()!! }

            readLine().let { line ->
                line.startsWith(SSE_FIELD_PREFIX_DATA)
                val flags = json.decodeFromString<List<Flag>>(
                    line.substring(SSE_FIELD_PREFIX_DATA.length)
                )
                assert(flags == emptyList<Flag>())
            }
            readLine().startsWith(SSE_FIELD_PREFIX_ID)
            readLine().isEmpty()

            val flag = this@withTestApplication.application.database.run {
                flags.run {
                    val id = capturingLastInsertId {
                        insert(project_id = projectId, name = NAME, rule = RULE)
                    }
                    select(id).executeAsOne()
                }
            }

            readLine().let { line ->
                line.startsWith(SSE_FIELD_PREFIX_DATA)
                val flags = json.decodeFromString<List<Flag>>(
                    line.substring(SSE_FIELD_PREFIX_DATA.length)
                )
                assert(flags == listOf(flag))
            }
            readLine().startsWith(SSE_FIELD_PREFIX_ID)
            readLine().isEmpty()
        }
    }

    @Test
    fun testFlagUpdate() = withTestApplication(Application::module) {
        val projectId = createProject(application)
        val id = application.database.capturingLastInsertId {
            flags.insert(project_id = projectId, name = NAME, rule = RULE)
        }
        assertResourceUpdates(PATH_FLAG(id), listOf("name" to NAME_UPDATED))
        var project = application.database.flags.select(id).executeAsOne()
        assert(project.id == id)
        assert(project.project_id == projectId)
        assert(project.name == NAME_UPDATED)
        assert(project.rule == RULE)
        assertResourceUpdates(PATH_FLAG(id), listOf("rule" to RULE_UPDATED))
        project = application.database.flags.select(id).executeAsOne()
        assert(project.id == id)
        assert(project.project_id == projectId)
        assert(project.name == NAME_UPDATED)
        assert(project.rule == RULE_UPDATED)
        assertResourceUpdates(PATH_FLAG(id), listOf("name" to NAME, "rule" to RULE))
        project = application.database.flags.select(id).executeAsOne()
        assert(project.id == id)
        assert(project.project_id == projectId)
        assert(project.name == NAME)
        assert(project.rule == RULE)
    }

    @Test
    fun testFlagDelete() = withTestApplication(Application::module) {
        val projectId = createProject(application)
        val id = application.database.capturingLastInsertId {
            flags.insert(project_id = projectId, name = NAME, rule = RULE)
        }
        assertResourceDeletes(PATH_FLAG(id))
        val flag = application.database.flags.select(id).executeAsOneOrNull()
        assert(flag == null)
    }

    private fun createProject(application: Application): Long {
        val organizationId = application.database.capturingLastInsertId {
            organizations.insert(name = "test-organization")
        }
        return application.database.capturingLastInsertId {
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
