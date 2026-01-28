package io.github.iamnicknack.pjs.server

import io.github.iamnicknack.pjs.model.device.DeviceRegistry

/**
 * Lazy provider for the device registry.
 */
fun interface DeviceRegistryProvider {
    fun createDeviceRegistry(): DeviceRegistry
}