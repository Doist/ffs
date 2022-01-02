package doist.ffs.auth

import doist.ffs.db.TokenScope
import io.ktor.server.auth.Principal

/**
 * Marks authenticated access to a project.
 */
abstract class ProjectPrincipal(val id: Long) : Principal

/**
 * Marks authenticated API access.
 */
class TokenPrincipal(projectId: Long, val scope: TokenScope) : ProjectPrincipal(projectId)
