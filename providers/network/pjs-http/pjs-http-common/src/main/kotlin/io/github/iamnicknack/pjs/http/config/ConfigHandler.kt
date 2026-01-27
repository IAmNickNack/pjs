package io.github.iamnicknack.pjs.http.config

import io.github.iamnicknack.pjs.model.device.Device
import io.github.iamnicknack.pjs.model.device.DeviceConfig

/**
 * Generic interface for handling device configuration
 */
interface ConfigHandler<T : Device<T>> {

    /**
     * Create a new device
     * @param config the device configuration
     */
    suspend fun createDevice(deviceId: String, config: DeviceConfigPayload<T>): DeviceConfig<T>

    /**
     * Remove a device
     * @param deviceId the device id
     */
    suspend fun removeDevice(deviceId: String)

    /**
     * Get the device configuration
     * @param deviceId the device id
     */
    suspend fun getDevice(deviceId: String): DeviceConfig<T>

    /**
     * Device configuration payload
     * @param T the type of the device
     */
    interface DeviceConfigPayload<T : Device<T>> {
        fun asDeviceConfig(deviceId: String? = null): DeviceConfig<T>
    }
}