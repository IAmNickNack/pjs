package io.github.iamnicknack.pjs.server

import io.github.iamnicknack.pjs.model.device.DeviceRegistry

/**
 * Factory for the device registry.
 */
fun interface DeviceRegistryFactory {
    fun createDeviceRegistry(): DeviceRegistry
}