package doist.ffs.db

import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import org.junit.jupiter.api.Test
import kotlin.test.assertFails

internal class TokenTest {
    private var projectId: Long = -1
    private var otherProjectId: Long = -1
    private val testDatabase = Database(JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)).apply {
        val organizationId = capturingLastInsertId {
            organizations.insert(name = "test-name")
        }
        projectId = capturingLastInsertId {
            projects.insert(organization_id = organizationId, name = "test-name")
        }
        otherProjectId = capturingLastInsertId {
            projects.insert(organization_id = organizationId, name = "other-test-name")
        }
    }

    @Test
    fun tokenGeneratorFormat() {
        repeat(100) {
            assert(TokenGenerator.isFormatValid(TokenGenerator.generate(Permission.EVAL)))
            assert(TokenGenerator.isFormatValid(TokenGenerator.generate(Permission.READ)))
        }
    }

    @Test
    fun tokenGeneratorScope() {
        repeat(100) {
            assert(
                Permission.fromToken(TokenGenerator.generate(Permission.EVAL)) == Permission.EVAL
            )
            assert(
                Permission.fromToken(TokenGenerator.generate(Permission.READ)) == Permission.READ
            )
        }
    }

    @Test
    fun insertValid(): Unit = testDatabase.tokens.run {
        insert(token = TOKEN_EVAL, project_id = projectId, description = DESCRIPTION)
        val token = selectByProject(projectId).executeAsOne()
        assert(token.project_id == projectId)
        assert(token.description == DESCRIPTION)
    }

    @Test
    fun insertDuplicate(): Unit = testDatabase.tokens.run {
        insert(token = TOKEN_EVAL, project_id = projectId, description = DESCRIPTION)
        insert(token = TOKEN_READ, project_id = otherProjectId, description = DESCRIPTION)
        assertFails {
            insert(token = TOKEN_EVAL, project_id = projectId, description = OTHER_DESCRIPTION)
        }
        assertFails {
            insert(token = TOKEN_READ, project_id = projectId, description = DESCRIPTION)
        }
        assertFails {
            insert(token = TOKEN_EVAL, project_id = otherProjectId, description = DESCRIPTION)
        }
    }

    @Test
    fun select(): Unit = testDatabase.run {
        val id = capturingLastInsertId {
            tokens.insert(token = TOKEN_EVAL, project_id = projectId, description = DESCRIPTION)
        }
        val token = tokens.select(id = id).executeAsOne()
        assert(token.project_id == projectId)
        assert(token.description == DESCRIPTION)
    }

    @Test
    fun selectByProject(): Unit = testDatabase.tokens.run {
        insert(token = TOKEN_EVAL, project_id = projectId, description = DESCRIPTION)
        insert(token = TOKEN_READ, project_id = projectId, description = OTHER_DESCRIPTION)
        val tokens = selectByProject(projectId).executeAsList()
        assert(tokens.size == 2)
        assert(tokens.map { it.description }.containsAll(listOf(DESCRIPTION, OTHER_DESCRIPTION)))
        assert(tokens.all { it.project_id == projectId })
    }

    @Test
    fun selectProject(): Unit = testDatabase.tokens.run {
        assert(selectProjectIdByToken(token = TOKEN_EVAL).executeAsOneOrNull() == null)
        assert(selectProjectIdByToken(token = TOKEN_READ).executeAsOneOrNull() == null)

        val idEval = testDatabase.capturingLastInsertId {
            insert(token = TOKEN_EVAL, project_id = projectId, description = DESCRIPTION)
        }
        assert(selectProjectIdById(id = idEval).executeAsOne() == projectId)
        assert(selectProjectIdByToken(token = TOKEN_EVAL).executeAsOne() == projectId)
        assert(selectProjectIdByToken(token = TOKEN_READ).executeAsOneOrNull() == null)

        val idRead = testDatabase.capturingLastInsertId {
            insert(token = TOKEN_READ, project_id = projectId, description = DESCRIPTION)
        }
        assert(selectProjectIdById(id = idRead).executeAsOne() == projectId)
        assert(selectProjectIdByToken(token = TOKEN_EVAL).executeAsOne() == projectId)
        assert(selectProjectIdByToken(token = TOKEN_READ).executeAsOne() == projectId)
    }

    @Test
    fun delete(): Unit = testDatabase.tokens.run {
        val idEval = testDatabase.capturingLastInsertId {
            insert(token = TOKEN_EVAL, project_id = projectId, description = DESCRIPTION)
        }
        val idRead = testDatabase.capturingLastInsertId {
            insert(token = TOKEN_READ, project_id = projectId, description = DESCRIPTION)
        }
        assert(selectProjectIdById(id = idEval).executeAsOne() == projectId)
        assert(selectProjectIdById(id = idRead).executeAsOne() == projectId)
        assert(selectProjectIdByToken(token = TOKEN_EVAL).executeAsOne() == projectId)
        assert(selectProjectIdByToken(token = TOKEN_READ).executeAsOne() == projectId)

        delete(id = idEval)
        assert(selectProjectIdById(id = idEval).executeAsOneOrNull() == null)
        assert(selectProjectIdById(id = idRead).executeAsOne() == projectId)
        assert(selectProjectIdByToken(token = TOKEN_EVAL).executeAsOneOrNull() == null)
        assert(selectProjectIdByToken(token = TOKEN_READ).executeAsOne() == projectId)

        delete(id = idRead)
        assert(selectProjectIdById(id = idEval).executeAsOneOrNull() == null)
        assert(selectProjectIdById(id = idRead).executeAsOneOrNull() == null)
        assert(selectProjectIdByToken(token = TOKEN_EVAL).executeAsOneOrNull() == null)
        assert(selectProjectIdByToken(token = TOKEN_READ).executeAsOneOrNull() == null)
    }

    companion object {
        private val TOKEN_EVAL = TokenGenerator.generate(Permission.EVAL)
        private val TOKEN_READ = TokenGenerator.generate(Permission.READ)

        private const val DESCRIPTION = "test token"
        private const val OTHER_DESCRIPTION = "other test token"
    }
}
