package io.github.iamnicknack.pjs.krpc.service

import io.github.iamnicknack.pjs.device.spi.Spi
import io.github.iamnicknack.pjs.krpc.asDataResponse
import io.github.iamnicknack.pjs.krpc.deviceOrThrow
import io.github.iamnicknack.pjs.grpc.gen.v1.spi.SpiService
import io.github.iamnicknack.pjs.grpc.gen.v1.types.DataRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.types.DataResponse
import io.github.iamnicknack.pjs.model.device.DeviceRegistry

class SpiServiceImpl(
    private val deviceRegistry: DeviceRegistry
) : SpiService {

    override suspend fun transfer(message: DataRequest): DataResponse {
        val device = deviceRegistry.deviceOrThrow<Spi>(message.deviceId)
        val bytesIn = ByteArray(message.payload.size)
        device.transfer(message.payload.toByteArray(), bytesIn)
        return bytesIn.asDataResponse()
    }
}