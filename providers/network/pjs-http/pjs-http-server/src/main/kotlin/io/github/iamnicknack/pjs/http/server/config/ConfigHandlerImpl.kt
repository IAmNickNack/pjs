package io.github.iamnicknack.pjs.http.server.config

import io.github.iamnicknack.pjs.http.config.ConfigHandler
import io.github.iamnicknack.pjs.http.config.ConfigHandler.DeviceConfigPayload
import io.github.iamnicknack.pjs.http.server.DeviceNotFoundException
import io.github.iamnicknack.pjs.http.server.cannotContain
import io.github.iamnicknack.pjs.model.device.Device
import io.github.iamnicknack.pjs.model.device.DeviceConfig
import io.github.iamnicknack.pjs.model.device.DeviceRegistry

class ConfigHandlerImpl<T : Device<T>>(
    private val deviceRegistry: DeviceRegistry,
    val deviceClass: Class<T>,
) : ConfigHandler<T> {

    /**
     * The handler currently needs access to the user-visible close handler to ensure [Device.close] removes
     * the device from the registry.
     */
    private val localDeviceCache: MutableMap<String, Device<T>> = mutableMapOf()

    override suspend  fun createDevice(deviceId: String, config: DeviceConfigPayload<T>): DeviceConfig<T> {
        deviceRegistry.cannotContain(deviceId)
        val device = deviceRegistry.create(config.asDeviceConfig(deviceId))
        localDeviceCache[deviceId] = device
        return device.config
    }

    override suspend  fun removeDevice(deviceId: String) {
        localDeviceCache[deviceId]
            ?.also { it.close() }
            ?: throw DeviceNotFoundException(deviceId)
    }

    override suspend  fun getDevice(deviceId: String): DeviceConfig<T> {
        return deviceRegistry.device(deviceId, deviceClass)
            ?.config
            ?: throw DeviceNotFoundException(deviceId)
    }
}