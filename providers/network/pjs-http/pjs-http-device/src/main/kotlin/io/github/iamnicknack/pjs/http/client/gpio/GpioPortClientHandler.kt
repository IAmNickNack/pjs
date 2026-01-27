package io.github.iamnicknack.pjs.http.client.gpio

import io.github.iamnicknack.pjs.device.gpio.GpioPort
import io.github.iamnicknack.pjs.http.gpio.GpioPortHandler
import io.github.iamnicknack.pjs.model.event.GpioEventListener

interface GpioPortClientHandler : GpioPortHandler {

    /**
     * Activate event listening for the device
     * @param deviceId the device id
     * @param listener the event listener
     */
    suspend fun listen(deviceId: String, listener: GpioEventListener<GpioPort>)

    /**
     * Deactivate event listening for the device
     * @param deviceId the device id
     */
    suspend fun unlisten(deviceId: String)
}