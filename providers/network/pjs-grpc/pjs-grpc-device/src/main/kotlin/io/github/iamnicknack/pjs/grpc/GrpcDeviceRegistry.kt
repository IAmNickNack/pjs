package io.github.iamnicknack.pjs.grpc

import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig
import io.github.iamnicknack.pjs.device.i2c.I2CConfig
import io.github.iamnicknack.pjs.device.pwm.PwmConfig
import io.github.iamnicknack.pjs.device.spi.SpiConfig
import io.github.iamnicknack.pjs.impl.DefaultDeviceRegistry
import io.github.iamnicknack.pjs.model.device.Device
import io.grpc.Channel
import io.grpc.ManagedChannelBuilder

/**
 * A registry of devices that is backed by gRPC.
 * @param channel the gRPC channel to use for communication with the device service.
 */
class GrpcDeviceRegistry(
    channel: Channel
) : DefaultDeviceRegistry() {

    constructor(
        host: String,
        port: Int
    ) : this(ManagedChannelBuilder.forAddress(host, port).usePlaintext().build())

    init {
        registerFactory(GrpcGpioPortFactory(channel), GpioPortConfig::class.java)
        registerFactory(GrpcPwmFactory(channel), PwmConfig::class.java)
        registerFactory(GrpcSpiFactory(channel), SpiConfig::class.java)
        registerFactory(GrpcI2CFactory(channel), I2CConfig::class.java)
    }

    override fun iterator(): MutableIterator<Device<*>?> {
        return super.iterator()
    }
}
