package io.github.iamnicknack.pjs.krpc

import io.github.iamnicknack.pjs.device.gpio.GpioPort
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig
import io.github.iamnicknack.pjs.device.gpio.GpioPortFactory
import io.github.iamnicknack.pjs.krpc.gen.v1.port.PortConfigService
import io.github.iamnicknack.pjs.krpc.gen.v1.port.PortService
import io.github.iamnicknack.pjs.krpc.proto.asGpioPortConfig
import io.github.iamnicknack.pjs.krpc.proto.asPortConfigPayload
import kotlinx.coroutines.runBlocking

class KrpcGpioPortFactory(
    private val portService: PortService,
    private val portConfigService: PortConfigService,
) : GpioPortFactory {

    override fun create(config: GpioPortConfig): GpioPort = runBlocking {
        val created = portConfigService.create(config.asPortConfigPayload())
        KrpcGpioPort(created.asGpioPortConfig(), portService, portConfigService)
    }
}