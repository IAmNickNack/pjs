package io.github.iamnicknack.pjs.grpc.service

import io.github.iamnicknack.pjs.device.spi.Spi
import io.github.iamnicknack.pjs.grpc.asDataResponse
import io.github.iamnicknack.pjs.grpc.deviceOrThrow
import io.github.iamnicknack.pjs.grpc.gen.v1.spi.SpiServiceGrpcKt
import io.github.iamnicknack.pjs.grpc.gen.v1.types.DataRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.types.DataResponse
import io.github.iamnicknack.pjs.model.device.DeviceRegistry

class GrpcSpiService(
    private val deviceRegistry: DeviceRegistry
) : SpiServiceGrpcKt.SpiServiceCoroutineImplBase() {

    override suspend fun transfer(request: DataRequest): DataResponse {
        val device = deviceRegistry.deviceOrThrow<Spi>(request.deviceId)
        val bytesIn = ByteArray(request.payload.size())
        device.transfer(request.payload.toByteArray(), bytesIn)
        return bytesIn.asDataResponse()
    }
}