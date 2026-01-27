package io.github.iamnicknack.pjs.grpc

import io.github.iamnicknack.pjs.device.spi.Spi
import io.github.iamnicknack.pjs.device.spi.SpiConfig
import io.github.iamnicknack.pjs.grpc.gen.v1.spi.SpiConfigServiceGrpc
import io.github.iamnicknack.pjs.grpc.gen.v1.spi.SpiServiceGrpc
import io.github.iamnicknack.pjs.model.device.DeviceConfig

class GrpcSpi(
    private val config: SpiConfig,
    private val stub : SpiServiceGrpc.SpiServiceBlockingStub,
    private val configStub: SpiConfigServiceGrpc.SpiConfigServiceBlockingStub,
) : Spi {

    override fun getConfig(): DeviceConfig<Spi> {
        return this.config
    }

    override fun transfer(write: ByteArray, writeOffset: Int, read: ByteArray, readOffset: Int, length: Int): Int {
        val response = stub.transfer(config.asDataRequest(write, writeOffset, length))
        response.payload.toByteArray().copyInto(read, readOffset, 0, length)
        return length
    }

    override fun close() {
        configStub.remove(config.asDeviceRequest())
    }
}