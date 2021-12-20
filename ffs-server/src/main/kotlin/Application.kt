package doist.ffs

import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import doist.ffs.plugins.Database
import doist.ffs.routes.installFlagRoutes
import doist.ffs.routes.installOrganizationRoutes
import doist.ffs.routes.installProjectRoutes
import doist.ffs.serialization.cbor
import doist.ffs.serialization.json
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.serialization
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.cio.EngineMain
import io.ktor.server.plugins.CallLogging
import io.ktor.server.plugins.Compression
import io.ktor.server.plugins.ContentNegotiation
import io.ktor.server.plugins.DefaultHeaders

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    // Install and configure plugins.
    install(Database) {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    }
    install(CallLogging)
    install(DefaultHeaders)
    install(ContentNegotiation) {
        serialization(ContentType.Application.Json, json)
        serialization(ContentType.Application.Cbor, cbor)
    }
    install(Compression)

    // Setup routes.
    installOrganizationRoutes()
    installProjectRoutes()
    installFlagRoutes()
}
