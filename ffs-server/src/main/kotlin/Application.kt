package doist.ffs

import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import doist.ffs.plugins.Database
import doist.ffs.routes.flagRoutes
import doist.ffs.routes.organizationRoutes
import doist.ffs.routes.projectRoutes
import doist.ffs.serialization.FlowConverter
import doist.ffs.serialization.json
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.serialization.json
import io.ktor.server.cio.EngineMain

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    // Install and configure plugins.
    install(Database) {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    }
    install(CallLogging)
    install(DefaultHeaders)
    install(ContentNegotiation) {
        json(json)
        register(ContentType.Text.EventStream, FlowConverter())
    }
    install(Compression)

    // Setup routes.
    organizationRoutes()
    projectRoutes()
    flagRoutes()
}
