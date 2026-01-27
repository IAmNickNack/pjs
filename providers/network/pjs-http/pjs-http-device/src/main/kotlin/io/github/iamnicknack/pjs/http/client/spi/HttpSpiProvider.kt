package io.github.iamnicknack.pjs.http.client.spi

import io.github.iamnicknack.pjs.device.spi.Spi
import io.github.iamnicknack.pjs.device.spi.SpiConfig
import io.github.iamnicknack.pjs.device.spi.SpiProvider
import io.github.iamnicknack.pjs.device.spi.SpiTransfer
import io.github.iamnicknack.pjs.http.spi.SpiHandler
import io.github.iamnicknack.pjs.http.spi.SpiTransferHandler
import kotlinx.coroutines.runBlocking

class HttpSpiProvider(
    private val spiHandler: SpiHandler,
    private val spiTransferHandler: SpiTransferHandler
) : SpiProvider {

    override fun create(config: SpiConfig): Spi {
        val config = runBlocking { spiHandler.createDevice(config.id, config.asSpiConfigPayload()) }
        return HttpSpi(spiHandler, config as SpiConfig)
    }

    override fun createTransfer(spi: Spi): SpiTransfer {
        return HttpSpiTransfer(spiTransferHandler, spi.config as SpiConfig)
    }

    fun SpiConfig.asSpiConfigPayload() = SpiHandler.SpiConfigPayload(
        baudRate,
        bus,
        chipSelect,
        mode,
        bitsPerWord,
        lsbFirst
    )
}