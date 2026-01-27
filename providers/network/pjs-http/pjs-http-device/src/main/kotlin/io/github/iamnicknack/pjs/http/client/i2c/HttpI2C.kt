package io.github.iamnicknack.pjs.http.client.i2c

import io.github.iamnicknack.pjs.device.i2c.I2C
import io.github.iamnicknack.pjs.device.i2c.I2CConfig
import io.github.iamnicknack.pjs.http.i2c.I2CHandler
import io.github.iamnicknack.pjs.model.device.DeviceConfig
import kotlinx.coroutines.runBlocking

class HttpI2C(
    private val i2cHandler: I2CHandler,
    private val config: I2CConfig
) : I2C {
    override fun transfer(vararg messages: I2C.Message) {
        val payload = I2CHandler.I2CTransferPayload(
            messages.map { I2CHandler.I2CTransferMessage(it.address, it.data, it.type) }
        )
        return runBlocking { i2cHandler.transfer(config.id, payload) }
    }

    override fun getConfig(): DeviceConfig<I2C> = config

    override fun close() = runBlocking {
        i2cHandler.removeDevice(config.id)
    }
}