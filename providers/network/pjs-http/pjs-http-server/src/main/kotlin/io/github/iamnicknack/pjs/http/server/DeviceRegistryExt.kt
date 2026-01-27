package io.github.iamnicknack.pjs.http.server

import io.github.iamnicknack.pjs.model.device.Device
import io.github.iamnicknack.pjs.model.device.DeviceRegistry

/**
 * Reified version of [DeviceRegistry.device]
 * @return device or null if device is not found
 */
inline fun <reified T : Device<T>> DeviceRegistry.device(deviceId: String): T? {
    return this.device(deviceId, T::class.java)
}

/**
 * Reified version of [DeviceRegistry.device]
 * @return device
 * @throws DeviceNotFoundException if the device is not found
 */
inline fun <reified T : Device<T>> DeviceRegistry.deviceOrThrow(deviceId: String): T {
    return this.device(deviceId, T::class.java) ?: throw DeviceNotFoundException(deviceId)
}

/**
 * Throws [DeviceAlreadyExistException] if the device is already present in the registry
 */
fun DeviceRegistry.cannotContain(deviceId: String) {
    if (this.contains(deviceId)) {
        throw DeviceAlreadyExistException(deviceId)
    }
}