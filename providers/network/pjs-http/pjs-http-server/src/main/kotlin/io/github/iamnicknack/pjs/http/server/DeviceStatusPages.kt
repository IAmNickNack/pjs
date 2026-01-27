package io.github.iamnicknack.pjs.http.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory

class DeviceNotFoundException(deviceId: String) : RuntimeException("Device not found: $deviceId")

class DeviceAlreadyExistException(deviceId: String) : RuntimeException("Device already exist: $deviceId")

fun Application.installStatusPages() {
    val logger = LoggerFactory.getLogger("DeviceStatusPages")
    install(StatusPages) {
        exception<DeviceNotFoundException> { call, e ->
            logger.warn("Device not found: ${e.message}")
            call.respond(HttpStatusCode.NotFound, e.message ?: "NotFound")
        }
        exception<DeviceAlreadyExistException> { call, e ->
            logger.warn("Device already exists: ${e.message}")
            call.respond(HttpStatusCode.Conflict, e.message ?: "Conflict")
        }
        exception<IllegalArgumentException> { call, e ->
            logger.warn("Illegal argument: ${e.message}")
            call.respond(HttpStatusCode.BadRequest, e.message ?: "Bad Request")
        }
        exception<Throwable> { call, e ->
            logger.error("Internal error: ${e.message}", e)
            call.respond(HttpStatusCode.InternalServerError, e.message ?: "Internal Server Error")
        }
    }
}