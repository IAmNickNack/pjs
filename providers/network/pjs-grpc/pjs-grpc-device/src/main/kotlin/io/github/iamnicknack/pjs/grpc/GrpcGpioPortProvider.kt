package io.github.iamnicknack.pjs.grpc

import io.github.iamnicknack.pjs.device.gpio.GpioPort
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig
import io.github.iamnicknack.pjs.device.gpio.GpioPortProvider
import io.github.iamnicknack.pjs.grpc.gen.v1.port.PortConfigServiceGrpc
import io.github.iamnicknack.pjs.grpc.gen.v1.port.PortServiceGrpc
import io.grpc.Channel

class GrpcGpioPortProvider(channel: Channel) : GpioPortProvider {

    private val portStub = PortServiceGrpc.newBlockingStub(channel)
    private val configStub = PortConfigServiceGrpc.newBlockingStub(channel)

    override fun create(config: GpioPortConfig): GpioPort {
        val created = configStub.create(config.asPortConfigPayload())
        return GrpcGpioPort(created.asGpioPortConfig(), portStub, configStub)
    }
}
