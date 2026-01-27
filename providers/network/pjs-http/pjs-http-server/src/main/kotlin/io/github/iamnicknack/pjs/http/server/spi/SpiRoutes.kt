package io.github.iamnicknack.pjs.http.server.spi

import io.github.iamnicknack.pjs.http.server.config.configRoutes
import io.github.iamnicknack.pjs.http.server.deviceId
import io.github.iamnicknack.pjs.http.spi.SpiHandler
import io.github.iamnicknack.pjs.http.spi.SpiTransferHandler
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.spiRoutes(handler: SpiHandler, transferHandler: SpiTransferHandler) {
    route("/api/v1/spi/{deviceId}") {

        /**
         * Perform an SPI transfer
         */
        post("/transfer") {
            val result = handler.transfer(call.deviceId, call.receive())
            call.respond(result)
        }

        post("/spi-transfer") {
            val result = transferHandler.transfer(call.deviceId, call.receive())
            call.respond(result)
        }

        configRoutes(
            handler,
            { call.receive(SpiHandler.SpiConfigPayload::class) },
            { call.respond(it) }
        )
    }
}