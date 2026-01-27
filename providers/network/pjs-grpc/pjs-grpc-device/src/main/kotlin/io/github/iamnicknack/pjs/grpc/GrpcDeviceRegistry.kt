package io.github.iamnicknack.pjs.grpc

import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig
import io.github.iamnicknack.pjs.device.i2c.I2CConfig
import io.github.iamnicknack.pjs.device.pwm.PwmConfig
import io.github.iamnicknack.pjs.device.spi.SpiConfig
import io.github.iamnicknack.pjs.impl.DefaultDeviceRegistry
import io.github.iamnicknack.pjs.model.device.Device
import io.grpc.Channel

/**
 * A registry of devices that is backed by gRPC.
 * @param channel the gRPC channel to use for communication with the device service.
 */
class GrpcDeviceRegistry(
    channel: Channel
) : DefaultDeviceRegistry() {

    init {
        registerProvider(GrpcGpioPortProvider(channel), GpioPortConfig::class.java)
        registerProvider(GrpcPwmProvider(channel), PwmConfig::class.java)
        registerProvider(GrpcSpiProvider(channel), SpiConfig::class.java)
        registerProvider(GrpcI2CProvider(channel), I2CConfig::class.java)
    }

    override fun iterator(): MutableIterator<Device<*>?> {
        return super.iterator()
    }
}
