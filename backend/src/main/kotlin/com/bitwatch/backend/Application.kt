package com.bitwatch.backend

import com.bitwatch.backend.config.configureSerialization
import com.bitwatch.backend.config.configureStatusPages
import com.bitwatch.backend.route.mobileRoutes
import com.bitwatch.backend.route.healthRoutes
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureSerialization()
        configureStatusPages()

        healthRoutes()
        mobileRoutes()
    }.start(wait = true)
}