package io.github.iamnicknack.pjs.http.client.spi

import io.github.iamnicknack.pjs.device.spi.Spi
import io.github.iamnicknack.pjs.device.spi.SpiConfig
import io.github.iamnicknack.pjs.device.spi.SpiFactory
import io.github.iamnicknack.pjs.http.spi.SpiHandler
import io.github.iamnicknack.pjs.http.spi.SpiTransferHandler
import kotlinx.coroutines.runBlocking

class HttpSpiFactory(
    private val spiHandler: SpiHandler,
    private val spiTransferHandler: SpiTransferHandler
) : SpiFactory {

    override fun create(config: SpiConfig): Spi {
        val config = runBlocking { spiHandler.createDevice(config.id, config.asSpiConfigPayload()) }
        return HttpSpi.Default(spiHandler, spiTransferHandler, config as SpiConfig)
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