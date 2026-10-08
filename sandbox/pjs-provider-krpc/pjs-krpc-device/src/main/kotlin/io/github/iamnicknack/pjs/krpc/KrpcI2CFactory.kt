package io.github.iamnicknack.pjs.krpc

import io.github.iamnicknack.pjs.device.i2c.I2C
import io.github.iamnicknack.pjs.device.i2c.I2CConfig
import io.github.iamnicknack.pjs.device.i2c.I2CFactory
import io.github.iamnicknack.pjs.krpc.gen.v1.i2c.bus.I2CBusConfigService
import io.github.iamnicknack.pjs.krpc.gen.v1.i2c.bus.I2CBusService
import io.github.iamnicknack.pjs.krpc.proto.asI2CBusConfig
import io.github.iamnicknack.pjs.krpc.proto.asI2CBusConfigPayload
import kotlinx.coroutines.runBlocking

class KrpcI2CFactory(
    private val i2cService: I2CBusService,
    private val i2cConfigService: I2CBusConfigService,
) : I2CFactory {

    override fun create(config: I2CConfig): I2C = runBlocking {
        val created = i2cConfigService.create(config.asI2CBusConfigPayload())
        KrpcI2C(created.asI2CBusConfig(), i2cService, i2cConfigService)
    }
}