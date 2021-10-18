package doist.ffs.plugins

import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import doist.ffs.db.Database
import io.ktor.application.Application
import io.ktor.application.ApplicationEvents
import io.ktor.application.ApplicationFeature
import io.ktor.application.ApplicationStopped
import io.ktor.application.feature
import io.ktor.application.log
import io.ktor.util.AttributeKey
import org.slf4j.Logger

/**
 * Plugin that opens a database connection on application start, and
 */
class Database(log: Logger, monitor: ApplicationEvents, configuration: Configuration) {
    var instance: doist.ffs.Database = Database(configuration.driver, log)

    init {
        monitor.subscribe(ApplicationStopped) {
            configuration.driver.close()
        }
    }

    class Configuration {
        var driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    }

    companion object Feature : ApplicationFeature<Application, Configuration, Database> {
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

val Application.database: doist.ffs.Database
    get() = feature(Database).instance
