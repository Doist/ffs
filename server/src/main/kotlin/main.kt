package doist.ffs

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer

fun main() {
    embeddedServer(CIO, port = 8080) {
        routing {
            get("/") {
                call.respond(HttpStatusCode.OK)
            }
        }
    }.start(wait = true)
}
