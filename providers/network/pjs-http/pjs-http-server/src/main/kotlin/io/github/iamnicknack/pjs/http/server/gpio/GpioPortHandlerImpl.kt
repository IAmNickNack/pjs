package io.github.iamnicknack.pjs.http.server.gpio

import io.github.iamnicknack.pjs.device.gpio.GpioPort
import io.github.iamnicknack.pjs.http.config.ConfigHandler
import io.github.iamnicknack.pjs.http.event.EventBroadcaster
import io.github.iamnicknack.pjs.http.server.deviceOrThrow
import io.github.iamnicknack.pjs.model.device.DeviceRegistry

/**
 * Default implementation of [io.github.iamnicknack.pjs.http.gpio.GpioPortHandler]
 */
class GpioPortHandlerImpl(
    private val deviceRegistry: DeviceRegistry,
    private val configHandler: ConfigHandler<GpioPort>
) : GpioPortServerHandler, ConfigHandler<GpioPort> by configHandler {

    private val _deviceEvents: MutableMap<String, EventBroadcaster<GpioPort>> = mutableMapOf()

    override suspend fun readDevice(deviceId: String): Int =
        deviceRegistry.deviceOrThrow<GpioPort>(deviceId)
            .read()

    override suspend fun writeDevice(deviceId: String, value: Int) =
        deviceRegistry.deviceOrThrow<GpioPort>(deviceId)
            .write(value)

    override suspend fun listen(deviceId: String) {
        deviceRegistry.deviceOrThrow<GpioPort>(deviceId)
            .addListener(eventBroadcasterForDevice(deviceId))
    }

    override suspend fun unlisten(deviceId: String) {
        _deviceEvents.remove(deviceId)?.also {
            it.close()
            deviceRegistry.deviceOrThrow<GpioPort>(deviceId)
                .removeListener(it)
        }
    }

    override fun eventBroadcasterForDevice(deviceId: String): EventBroadcaster<GpioPort> =
        _deviceEvents.computeIfAbsent(deviceId) { EventBroadcaster.ServerChannel() }

}