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
    fun testTokenGeneratorFormat() {
        repeat(100) {
            for (scope in TokenScope.VALUES) {
                assert(TokenGenerator.isFormatValid(TokenGenerator.generate(scope)))
            }
        }
    }

    @Test
    fun testTokenGeneratorScope() {
        repeat(100) {
            for (scope in TokenScope.VALUES) {
                assert(scope.includes(TokenGenerator.generate(scope)))
            }
        }
    }

    @Test
    fun testInsertValid(): Unit = testDatabase.tokens.run {
        insert(token = TOKEN_EVAL, project_id = projectId, description = DESCRIPTION)
        val token = selectByProject(projectId).executeAsList()[0]
        assert(token.project_id == projectId)
        assert(token.description == DESCRIPTION)
    }

    @Test
    fun testInsertDuplicate(): Unit = testDatabase.tokens.run {
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
    fun testSelectByProject(): Unit = testDatabase.tokens.run {
        insert(token = TOKEN_EVAL, project_id = projectId, description = DESCRIPTION)
        insert(token = TOKEN_READ, project_id = projectId, description = OTHER_DESCRIPTION)
        val tokens = selectByProject(projectId).executeAsList()
        assert(tokens.size == 2)
        assert(tokens.map { it.description }.containsAll(listOf(DESCRIPTION, OTHER_DESCRIPTION)))
        assert(tokens.all { it.project_id == projectId })
    }

    @Test
    fun testSelectProject(): Unit = testDatabase.tokens.run {
        assert(selectProjectIdByToken(token = TOKEN_EVAL).executeAsOneOrNull() == null)
        assert(selectProjectIdByToken(token = TOKEN_READ).executeAsOneOrNull() == null)

        insert(token = TOKEN_EVAL, project_id = projectId, description = DESCRIPTION)
        assert(selectProjectIdByToken(token = TOKEN_EVAL).executeAsOne() == projectId)
        assert(selectProjectIdByToken(token = TOKEN_READ).executeAsOneOrNull() == null)

        insert(token = TOKEN_READ, project_id = projectId, description = DESCRIPTION)
        assert(selectProjectIdByToken(token = TOKEN_EVAL).executeAsOne() == projectId)
        assert(selectProjectIdByToken(token = TOKEN_READ).executeAsOne() == projectId)
    }

    @Test
    fun testDelete(): Unit = testDatabase.tokens.run {
        val idEval = testDatabase.capturingLastInsertId {
            insert(token = TOKEN_EVAL, project_id = projectId, description = DESCRIPTION)
        }
        val idRead = testDatabase.capturingLastInsertId {
            insert(token = TOKEN_READ, project_id = projectId, description = DESCRIPTION)
        }
        assert(selectProjectIdByToken(token = TOKEN_EVAL).executeAsOne() == projectId)
        assert(selectProjectIdByToken(token = TOKEN_READ).executeAsOne() == projectId)

        delete(idEval)
        assert(selectProjectIdByToken(token = TOKEN_EVAL).executeAsOneOrNull() == null)
        assert(selectProjectIdByToken(token = TOKEN_READ).executeAsOne() == projectId)

        delete(idRead)
        assert(selectProjectIdByToken(token = TOKEN_EVAL).executeAsOneOrNull() == null)
        assert(selectProjectIdByToken(token = TOKEN_READ).executeAsOneOrNull() == null)
    }

    companion object {
        private val TOKEN_EVAL = TokenGenerator.generate(TokenScope.SCOPE_EVAL)
        private val TOKEN_READ = TokenGenerator.generate(TokenScope.SCOPE_READ)

        private const val DESCRIPTION = "test token"
        private const val OTHER_DESCRIPTION = "other test token"
    }
}
