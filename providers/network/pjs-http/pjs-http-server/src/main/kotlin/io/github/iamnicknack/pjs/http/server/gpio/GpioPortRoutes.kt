package io.github.iamnicknack.pjs.http.server.gpio

import io.github.iamnicknack.pjs.http.gpio.GpioPortHandler
import io.github.iamnicknack.pjs.http.server.config.configRoutes
import io.github.iamnicknack.pjs.http.server.deviceId
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.ktor.sse.*
import io.ktor.util.date.*

fun Route.gpioPortRoutes(
    handler: GpioPortServerHandler,
) {
    route("/api/v1/gpio/{deviceId}") {
        /**
         * Read the current value of the port
         */
        get("/value") {
            call.respond(handler.readDevice(call.deviceId))
        }

        /**
         * Set the value of the port
         */
        put("/value/{value}") {
            val value = call.parameters["value"]?.toInt() ?: throw IllegalArgumentException("Invalid value")
            handler.writeDevice(call.deviceId, value)
            call.respond(HttpStatusCode.OK)
        }

        configRoutes(
            handler,
            { call.receive(GpioPortHandler.GpioPortConfigPayload::class) },
            { call.respond(it) }
        )

        /**
         * Unsubscribe from state change events
         */
        delete("/events") {
            handler.unlisten(call.deviceId)
            call.respond(HttpStatusCode.OK)
        }

        /**
         * Subscribe to state change events from the device
         */
        sse("/events") {
            val deviceId = call.parameters["deviceId"] ?: throw IllegalArgumentException("Invalid deviceId")
            handler.listen(deviceId)

            send(ServerSentEvent(event = GpioPortHandler.SseEventType.CONNECTED.toString()))

            handler.eventBroadcasterForDevice(deviceId)
                .events
                .collect { event ->
                    ServerSentEvent(
                        data = event.port.config.id,
                        event = event.eventType.toString(),
                        id = getTimeMillis().toString()
                    )
                    .also { send(it) }
                }

        }
    }
}