package io.github.iamnicknack.pjs.grpc

import io.github.iamnicknack.pjs.device.spi.Spi
import io.github.iamnicknack.pjs.device.spi.SpiConfig
import io.github.iamnicknack.pjs.device.spi.SpiFactory
import io.github.iamnicknack.pjs.device.spi.SpiTransfer
import io.github.iamnicknack.pjs.grpc.gen.v1.spi.SpiConfigServiceGrpc
import io.github.iamnicknack.pjs.grpc.gen.v1.spi.SpiServiceGrpc
import io.github.iamnicknack.pjs.grpc.gen.v1.spi.SpiTransferServiceGrpc
import io.grpc.Channel

class GrpcSpiFactory(
    channel: Channel
) : SpiFactory {

    private val spiStub = SpiServiceGrpc.newBlockingStub(channel)
    private val configStub = SpiConfigServiceGrpc.newBlockingStub(channel)
    private val transferStub = SpiTransferServiceGrpc.newBlockingStub(channel)

    override fun create(config: SpiConfig): Spi {
        val created = configStub.create(config.asSpiConfigPayload())
        return GrpcSpi(created.asSpiConfig(), spiStub, configStub)
    }

    override fun createTransfer(spi: Spi): SpiTransfer {
        return GrpcSpiTransfer(spi.config as SpiConfig, transferStub)
    }
}