package doist.ffs.routes

import io.ktor.application.Application
import io.ktor.application.ApplicationStopped
import io.ktor.config.MapApplicationConfig
import kotlin.io.path.deleteIfExists

fun Application.configureDatabaseForLifecycleTest() {
    val databasePath = kotlin.io.path.createTempFile()
    (environment.config as MapApplicationConfig).apply {
        put("database.path", databasePath.toString())
    }
    environment.monitor.subscribe(ApplicationStopped) {
        databasePath.deleteIfExists()
    }
}
