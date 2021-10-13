package doist.ffs

import doist.ffs.routes.organizationRoutes
import doist.ffs.routes.projectRoutes
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.serialization.json
import io.ktor.server.cio.EngineMain
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    // Parse environment.
    environment.config.propertyOrNull("database.path")?.let {
        setDefaultDatabasePath(it.getString())
    }

    // Install and configure plugins.
    install(CallLogging)
    install(DefaultHeaders)
    install(ContentNegotiation) {
        json(
            Json {
                isLenient = true
                serializersModule = SerializersModule {
                    contextual(OrganizationSerializer)
                    contextual(ProjectSerializer)
                }
            }
        )
    }

    // Setup routes.
    organizationRoutes()
    projectRoutes()
}
