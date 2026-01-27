package io.github.iamnicknack.pjs.http.server.config

import io.github.iamnicknack.pjs.http.config.ConfigHandler
import io.github.iamnicknack.pjs.http.server.deviceId
import io.github.iamnicknack.pjs.model.device.Device
import io.github.iamnicknack.pjs.model.device.DeviceConfig
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Generic routes for handling device configuration
 */
fun <T : Device<T>> Route.configRoutes(
    handler: ConfigHandler<T>,
    payloadReceiver: suspend RoutingContext.() -> ConfigHandler.DeviceConfigPayload<T>,
    configResponder: suspend RoutingContext.(DeviceConfig<T>) -> Unit
) {
    /**
     * Create a new device
     */
    post {
        val deviceId = call.deviceId
        val configPayload: ConfigHandler.DeviceConfigPayload<T> = payloadReceiver()
        val config = handler.createDevice(deviceId, configPayload)
        configResponder(config)
    }

    /**
     * Remove a device
     */
    delete {
        val deviceId = call.deviceId
        handler.removeDevice(deviceId)
        call.respond(HttpStatusCode.OK)
    }

    /**
     * Get the device configuration
     */
    get {
        val deviceId = call.deviceId
        configResponder(handler.getDevice(deviceId))
    }
}