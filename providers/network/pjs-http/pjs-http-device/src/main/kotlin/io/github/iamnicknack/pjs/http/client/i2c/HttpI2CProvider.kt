package io.github.iamnicknack.pjs.http.client.i2c

import io.github.iamnicknack.pjs.device.i2c.I2C
import io.github.iamnicknack.pjs.device.i2c.I2CConfig
import io.github.iamnicknack.pjs.device.i2c.I2CProvider
import io.github.iamnicknack.pjs.http.i2c.I2CHandler
import kotlinx.coroutines.runBlocking

class HttpI2CProvider(
    private val i2cHandler: I2CHandler
) : I2CProvider {
    override fun create(config: I2CConfig): I2C {
        val config = runBlocking {
            i2cHandler.createDevice(config.id, config.asI2CConfigPayload())
        }
        return HttpI2C(i2cHandler, config as I2CConfig)
    }

    fun I2CConfig.asI2CConfigPayload() = I2CHandler.I2CConfigPayload(bus)
}