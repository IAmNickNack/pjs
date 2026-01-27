package io.github.iamnicknack.pjs.http.server.spi

import io.github.iamnicknack.pjs.device.spi.Spi
import io.github.iamnicknack.pjs.http.config.ConfigHandler
import io.github.iamnicknack.pjs.http.server.deviceOrThrow
import io.github.iamnicknack.pjs.http.spi.SpiHandler
import io.github.iamnicknack.pjs.model.device.DeviceRegistry

/**
 * Default implementation of [io.github.iamnicknack.pjs.http.spi.SpiHandler]
 */
class SpiHandlerImpl(
    private val deviceRegistry: DeviceRegistry,
    private val configHandler: ConfigHandler<Spi>
) : SpiHandler, ConfigHandler<Spi> by configHandler {
    override suspend fun transfer(deviceId: String, payload: SpiHandler.TransferPayload): SpiHandler.TransferPayload {
        val bytes = ByteArray(payload.data.size)
        deviceRegistry.deviceOrThrow<Spi>(deviceId).transfer(payload.data, bytes)
        return SpiHandler.TransferPayload(bytes)
    }
}