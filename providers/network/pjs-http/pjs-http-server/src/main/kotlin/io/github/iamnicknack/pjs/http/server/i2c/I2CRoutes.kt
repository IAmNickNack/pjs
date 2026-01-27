package io.github.iamnicknack.pjs.http.server.i2c

import io.github.iamnicknack.pjs.http.i2c.I2CHandler
import io.github.iamnicknack.pjs.http.server.config.configRoutes
import io.github.iamnicknack.pjs.http.server.deviceId
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.i2cRoutes(handler: I2CHandler) {

    route("/api/v1/i2c/{deviceId}") {
        /**
         * Perform an I2C transfer
         */
        post("/transfer") {
            val deviceId = call.deviceId
            val payload: I2CHandler.I2CTransferPayload = call.receive()
            val result = handler.transfer(deviceId, payload)
            call.respond(result)
        }

        configRoutes(
            handler,
            { call.receive(I2CHandler.I2CConfigPayload::class) },
            { call.respond(it) }
        )
    }
}