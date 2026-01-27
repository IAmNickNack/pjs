package io.github.iamnicknack.pjs.http.client.spi

import io.github.iamnicknack.pjs.device.spi.Spi
import io.github.iamnicknack.pjs.device.spi.SpiConfig
import io.github.iamnicknack.pjs.http.spi.SpiHandler
import io.github.iamnicknack.pjs.model.device.DeviceConfig
import kotlinx.coroutines.runBlocking

class HttpSpi(
    private val spiHandler: SpiHandler,
    private val config: SpiConfig
) : Spi {
    override fun transfer(
        write: ByteArray,
        writeOffset: Int,
        read: ByteArray,
        readOffset: Int,
        length: Int
    ): Int {
        val payload = SpiHandler.TransferPayload(write.copyOfRange(writeOffset, writeOffset + length))
        val result = runBlocking { spiHandler.transfer(config.id, payload) }
        result.data.copyInto(read, readOffset, 0, length)
        return length
    }

    override fun getConfig(): DeviceConfig<Spi> = config

    override fun close() = runBlocking {
        spiHandler.removeDevice(config.id)
    }
}