package io.github.iamnicknack.pjs.grpc

import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig
import io.github.iamnicknack.pjs.device.i2c.I2CConfig
import io.github.iamnicknack.pjs.device.pwm.PwmConfig
import io.github.iamnicknack.pjs.device.spi.SpiConfig
import io.github.iamnicknack.pjs.model.device.Device
import io.github.iamnicknack.pjs.model.device.DeviceConfig
import io.github.iamnicknack.pjs.model.device.GenericDeviceFactory
import io.grpc.Channel

/**
 * Factory for creating devices backed by gRPC.
 * @param channel The gRPC channel to use for communication.
 */
class GrpcDeviceFactory(channel: Channel) : GenericDeviceFactory {

    private val factory = GenericDeviceFactory.builder()
        .factory(GrpcGpioPortFactory(channel), GpioPortConfig::class.java)
        .factory(GrpcPwmFactory(channel), PwmConfig::class.java)
        .factory(GrpcSpiFactory(channel), SpiConfig::class.java)
        .factory(GrpcI2CFactory(channel), I2CConfig::class.java)
        .build()

    override fun <T : Device<T>, V : DeviceConfig<T>> create(config: V): T = factory.create(config)
}
