package io.github.iamnicknack.pjs.krpc

import io.github.iamnicknack.pjs.device.spi.Spi
import io.github.iamnicknack.pjs.device.spi.SpiConfig
import io.github.iamnicknack.pjs.device.spi.SpiTransfer
import io.github.iamnicknack.pjs.krpc.gen.v1.spi.SpiConfigService
import io.github.iamnicknack.pjs.krpc.gen.v1.spi.SpiService
import io.github.iamnicknack.pjs.krpc.gen.v1.spi.SpiTransferService
import io.github.iamnicknack.pjs.krpc.proto.asDataRequest
import io.github.iamnicknack.pjs.krpc.proto.asDeviceRequest
import io.github.iamnicknack.pjs.model.device.DeviceConfig
import kotlinx.coroutines.runBlocking

class KrpcSpi(
    private val config: SpiConfig,
    private val spiService: SpiService,
    private val spiTransferService: SpiTransferService,
    private val spiConfigService: SpiConfigService,
) : Spi {
    override fun transfer(
        write: ByteArray,
        writeOffset: Int,
        read: ByteArray,
        readOffset: Int,
        length: Int
    ): Int = runBlocking {
        val response = spiService.transfer(config.asDataRequest(write, writeOffset, length))
        response.payload.copyInto(read, readOffset, length)
        response.payload.size
    }

    override fun createTransfer(): SpiTransfer = KrpcSpiTransfer(spiTransferService)

    override fun getConfig(): DeviceConfig<Spi> = config

    override fun close(): Unit = runBlocking {
        spiConfigService.remove(config.asDeviceRequest())
    }
}