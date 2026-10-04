package io.github.iamnicknack.pjs.krpc

import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig
import io.github.iamnicknack.pjs.device.i2c.I2CConfig
import io.github.iamnicknack.pjs.device.pwm.PwmConfig
import io.github.iamnicknack.pjs.device.spi.SpiConfig
import io.github.iamnicknack.pjs.grpc.gen.v1.i2c.bus.I2CBusConfigService
import io.github.iamnicknack.pjs.grpc.gen.v1.i2c.bus.I2CBusService
import io.github.iamnicknack.pjs.grpc.gen.v1.port.PortConfigService
import io.github.iamnicknack.pjs.grpc.gen.v1.port.PortService
import io.github.iamnicknack.pjs.grpc.gen.v1.pwm.PwmConfigService
import io.github.iamnicknack.pjs.grpc.gen.v1.pwm.PwmService
import io.github.iamnicknack.pjs.grpc.gen.v1.spi.SpiConfigService
import io.github.iamnicknack.pjs.grpc.gen.v1.spi.SpiService
import io.github.iamnicknack.pjs.grpc.gen.v1.spi.SpiTransferService
import io.github.iamnicknack.pjs.model.device.Device
import io.github.iamnicknack.pjs.model.device.DeviceConfig
import io.github.iamnicknack.pjs.model.device.GenericDeviceFactory
import kotlinx.rpc.grpc.client.GrpcClient
import kotlinx.rpc.withService

class KrpcDeviceFactory(client: GrpcClient) : GenericDeviceFactory {

    private val factory = GenericDeviceFactory.builder()
        .factory(
            KrpcGpioPortFactory(
                client.withService<PortService>(),
                client.withService<PortConfigService>()
            ),
            GpioPortConfig::class.java
        )
        .factory(
            KrpcI2CFactory(
                client.withService<I2CBusService>(),
                client.withService<I2CBusConfigService>()
            ),
            I2CConfig::class.java
        )
        .factory(
            KrpcPwmFactory(
                client.withService<PwmService>(),
                client.withService<PwmConfigService>()
            ),
            PwmConfig::class.java
        )
        .factory(
            KrpcSpiFactory(
                client.withService<SpiService>(),
                client.withService<SpiTransferService>(),
                client.withService<SpiConfigService>()
            ),
            SpiConfig::class.java
        )
        .build()

    override fun <T : Device<T>, V : DeviceConfig<T>> create(config: V): T = factory.create(config)
}