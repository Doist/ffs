package doist.ffs.plugins

import com.squareup.sqldelight.sqlite.driver.JdbcDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import doist.ffs.db.Database
import io.ktor.events.Events
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationPlugin
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.call
import io.ktor.server.application.log
import io.ktor.server.application.plugin
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import org.slf4j.Logger

/**
 * Plugin that opens a database connection on application start, and closes it on application stop.
 */
class Database(log: Logger, monitor: Events, configuration: Configuration) {
    var instance: doist.ffs.Database = Database(configuration.driver, log)

    init {
        monitor.subscribe(ApplicationStopped) {
            configuration.driver.close()
        }
    }

    class Configuration {
        var driver: JdbcDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    }

    companion object Plugin : ApplicationPlugin<Application, Configuration, Database> {
        override val key = AttributeKey<Database>("Database")

        override fun install(pipeline: Application, configure: Configuration.() -> Unit): Database {
            return Database(
                pipeline.log,
                pipeline.environment.monitor,
                Configuration().apply(configure)
            )
        }
    }
}

inline val Application.database: doist.ffs.Database
    get() = plugin(Database).instance

inline val PipelineContext<*, ApplicationCall>.database: doist.ffs.Database
    get() = call.application.database
