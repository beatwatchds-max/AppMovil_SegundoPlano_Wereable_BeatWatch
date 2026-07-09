package com.bitwatch.backend.route

import com.bitwatch.backend.model.SyncRequest
import com.bitwatch.backend.service.SyncService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.mobileRoutes() {
    val syncService = SyncService()

    routing {
        post("/api/sync") {
            val payload = call.receive<SyncRequest>()
            val result = syncService.processSync(payload)
            call.respond(HttpStatusCode.OK, result)
        }

        get("/api/events/{patientId}") {
            val patientId = call.parameters["patientId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing patientId")
            val events = syncService.getPatientEvents(patientId)
            call.respond(events)
        }
    }
}