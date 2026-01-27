package io.github.iamnicknack.pjs.grpc.service

import io.github.iamnicknack.pjs.device.spi.Spi
import io.github.iamnicknack.pjs.device.spi.SpiConfig
import io.github.iamnicknack.pjs.grpc.asSpiConfig
import io.github.iamnicknack.pjs.grpc.asSpiConfigPayload
import io.github.iamnicknack.pjs.grpc.cannotContain
import io.github.iamnicknack.pjs.grpc.deviceOrThrow
import io.github.iamnicknack.pjs.grpc.gen.v1.spi.SpiConfigListResponse
import io.github.iamnicknack.pjs.grpc.gen.v1.spi.SpiConfigPayload
import io.github.iamnicknack.pjs.grpc.gen.v1.spi.SpiConfigServiceGrpcKt
import io.github.iamnicknack.pjs.grpc.gen.v1.types.DeviceRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.types.Empty
import io.github.iamnicknack.pjs.model.device.DeviceRegistry

class GrpcSpiConfigService(
    private val deviceRegistry: DeviceRegistry
) : SpiConfigServiceGrpcKt.SpiConfigServiceCoroutineImplBase() {

    override suspend fun create(request: SpiConfigPayload): SpiConfigPayload {
        deviceRegistry.cannotContain(request.deviceId)
        val device = deviceRegistry.create(request.asSpiConfig())
        return (device.config as SpiConfig).asSpiConfigPayload()
    }

    override suspend fun fetchConfig(request: DeviceRequest): SpiConfigPayload {
        deviceRegistry.deviceOrThrow<Spi>(request.deviceId)
        return super.fetchConfig(request)
    }

    override suspend fun fetchDevices(request: Empty): SpiConfigListResponse {
        return deviceRegistry
            .mapNotNull { it.config as? SpiConfig }
            .fold(SpiConfigListResponse.newBuilder()) { builder, config ->
                builder.addConfig(config.asSpiConfigPayload())
            }
            .build()
    }

    override suspend fun remove(request: DeviceRequest): Empty {
        val device = deviceRegistry.deviceOrThrow<Spi>(request.deviceId)
        deviceRegistry.remove(device)
        return Empty.getDefaultInstance()
    }
}