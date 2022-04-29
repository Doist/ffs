package doist.ffs.plugins

import com.squareup.sqldelight.sqlite.driver.JdbcDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import doist.ffs.Database
import doist.ffs.db.Database
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.call
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.MonitoringEvent
import io.ktor.server.application.log
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext

private val DatabaseProviderKey = AttributeKey<Database>("DatabaseProviderKey")

/**
 * Plugin that opens a database connection on application start, and closes it on application stop.
 */
val Database = createApplicationPlugin(
    name = "Database",
    createConfiguration = ::DatabaseConfiguration
) {
    application.attributes.put(
        DatabaseProviderKey,
        Database(pluginConfig.driver, application.log)
    )

    on(MonitoringEvent(ApplicationStopped)) {
        application.attributes.remove(DatabaseProviderKey)
    }
}

class DatabaseConfiguration {
    var driver: JdbcDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
}

val Application.database: Database
    get() = attributes.getOrNull(DatabaseProviderKey)
        ?: error("Database not installed or application not started")

inline val PipelineContext<*, ApplicationCall>.database: Database
    get() = call.application.database
