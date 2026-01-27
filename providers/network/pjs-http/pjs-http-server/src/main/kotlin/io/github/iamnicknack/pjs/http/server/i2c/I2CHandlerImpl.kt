package io.github.iamnicknack.pjs.http.server.i2c

import io.github.iamnicknack.pjs.device.i2c.I2C
import io.github.iamnicknack.pjs.http.config.ConfigHandler
import io.github.iamnicknack.pjs.http.server.deviceOrThrow
import io.github.iamnicknack.pjs.http.i2c.I2CHandler
import io.github.iamnicknack.pjs.model.device.DeviceRegistry

class I2CHandlerImpl(
    private val deviceRegistry: DeviceRegistry,
    private val configHandler: ConfigHandler<I2C>
) : I2CHandler, ConfigHandler<I2C> by configHandler {

    override suspend fun transfer(
        deviceId: String,
        payload: I2CHandler.I2CTransferPayload
    ): I2CHandler.I2CTransferPayload {
        val device = deviceRegistry.deviceOrThrow<I2C>(deviceId)
        val messages = payload.messages.map { it.asI2CMessage() }

        device.transfer(*messages.toTypedArray())

        return payload
    }

    fun I2CHandler.I2CTransferMessage.asI2CMessage() = I2C.Message(
        address,
        payload,
        0,
        payload.size,
        type
    )
}