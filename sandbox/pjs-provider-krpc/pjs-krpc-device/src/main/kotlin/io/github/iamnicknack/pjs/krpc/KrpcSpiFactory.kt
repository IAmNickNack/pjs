package io.github.iamnicknack.pjs.krpc

import io.github.iamnicknack.pjs.device.spi.Spi
import io.github.iamnicknack.pjs.device.spi.SpiConfig
import io.github.iamnicknack.pjs.device.spi.SpiFactory
import io.github.iamnicknack.pjs.krpc.gen.v1.spi.SpiConfigService
import io.github.iamnicknack.pjs.krpc.gen.v1.spi.SpiService
import io.github.iamnicknack.pjs.krpc.gen.v1.spi.SpiTransferService
import io.github.iamnicknack.pjs.krpc.proto.asSpiConfig
import io.github.iamnicknack.pjs.krpc.proto.asSpiConfigPayload
import kotlinx.coroutines.runBlocking

class KrpcSpiFactory(
    private val spiService: SpiService,
    private val spiTransferService: SpiTransferService,
    private val spiConfigService: SpiConfigService
) : SpiFactory {

    override fun create(config: SpiConfig): Spi = runBlocking {
        val created = spiConfigService.create(config.asSpiConfigPayload()).asSpiConfig()
        KrpcSpi(created, spiService, spiTransferService, spiConfigService)
    }
}