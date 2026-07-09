package com.bitwatch.backend.route

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.healthRoutes() {
    routing {
        get("/health") {
            call.respond(mapOf("status" to "UP"))
        }
    }
}