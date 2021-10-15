package doist.ffs.routes

import doist.ffs.module
import io.ktor.application.ApplicationStopped
import io.ktor.config.MapApplicationConfig
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import kotlin.io.path.deleteIfExists

fun withTestApplication(block: TestApplicationEngine.() -> Unit) {
    withTestApplication({
        val databasePath = kotlin.io.path.createTempFile()
        (environment.config as MapApplicationConfig).apply {
            put("database.path", databasePath.toString())
        }
        environment.monitor.subscribe(ApplicationStopped) {
            databasePath.deleteIfExists()
        }
        module()
    }) {
        block()
    }
}
