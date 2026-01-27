package io.github.iamnicknack.pjs.http.server.config

import io.github.iamnicknack.pjs.http.server.DeviceNotFoundException
import io.github.iamnicknack.pjs.http.server.cannotContain
import io.github.iamnicknack.pjs.http.config.ConfigHandler
import io.github.iamnicknack.pjs.model.device.Device
import io.github.iamnicknack.pjs.model.device.DeviceConfig
import io.github.iamnicknack.pjs.model.device.DeviceRegistry

class ConfigHandlerImpl<T : Device<T>>(
    private val deviceRegistry: DeviceRegistry,
    val deviceClass: Class<T>,
) : ConfigHandler<T> {

    override suspend  fun createDevice(deviceId: String, config: ConfigHandler.DeviceConfigPayload<T>): DeviceConfig<T> {
        deviceRegistry.cannotContain(deviceId)
        val device = deviceRegistry.create(config.asDeviceConfig(deviceId))
        return device.config as DeviceConfig<T>
    }

    override suspend  fun removeDevice(deviceId: String) {
        deviceRegistry.device(deviceId, deviceClass)
            ?.also { deviceRegistry.remove(it) }
            ?: throw DeviceNotFoundException(deviceId)
    }

    override suspend  fun getDevice(deviceId: String): DeviceConfig<T> {
        return deviceRegistry.device(deviceId, deviceClass)
            ?.config
            ?: throw DeviceNotFoundException(deviceId)
    }
}