package io.github.iamnicknack.pjs.grpc

import io.github.iamnicknack.pjs.device.i2c.I2C
import io.github.iamnicknack.pjs.device.i2c.I2CConfig
import io.github.iamnicknack.pjs.device.i2c.I2CProvider
import io.github.iamnicknack.pjs.grpc.gen.v1.i2c.bus.I2CBusConfigServiceGrpc
import io.github.iamnicknack.pjs.grpc.gen.v1.i2c.bus.I2CBusServiceGrpc
import io.grpc.Channel

class GrpcI2CProvider(
    channel: Channel
) : I2CProvider {
    private val stub = I2CBusServiceGrpc.newBlockingStub(channel)
    private val configStub = I2CBusConfigServiceGrpc.newBlockingStub(channel)

    override fun create(config: I2CConfig): I2C {
        val created = configStub.create(config.asI2CBusConfigPayload())
        return GrpcI2C(created.asI2CBusConfig(), stub, configStub)
    }
}