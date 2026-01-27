package io.github.iamnicknack.pjs.http.server.gpio

import io.github.iamnicknack.pjs.device.gpio.GpioPort
import io.github.iamnicknack.pjs.http.event.EventBroadcaster
import io.github.iamnicknack.pjs.http.gpio.GpioPortHandler

interface GpioPortServerHandler : GpioPortHandler {

    /**
     * Activate event listening for the device
     * @param deviceId the device id
     */
    suspend fun listen(deviceId: String)

    /**
     * Deactivate event listening for the device
     * @param deviceId the device id
     */
    suspend fun unlisten(deviceId: String)

    /**
     * Create or fetch an event broadcaster for the device
     * @param deviceId the device id
     */
    fun eventBroadcasterForDevice(deviceId: String): EventBroadcaster<GpioPort>
}