package io.github.iamnicknack.pjs.grpc.service

import io.github.iamnicknack.pjs.device.gpio.GpioPort
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig
import io.github.iamnicknack.pjs.grpc.asGpioPortConfig
import io.github.iamnicknack.pjs.grpc.asPortConfigPayload
import io.github.iamnicknack.pjs.grpc.cannotContain
import io.github.iamnicknack.pjs.grpc.deviceOrThrow
import io.github.iamnicknack.pjs.grpc.gen.v1.port.Empty
import io.github.iamnicknack.pjs.grpc.gen.v1.port.PortConfigListResponse
import io.github.iamnicknack.pjs.grpc.gen.v1.port.PortConfigPayload
import io.github.iamnicknack.pjs.grpc.gen.v1.port.PortConfigServiceGrpcKt
import io.github.iamnicknack.pjs.grpc.gen.v1.types.DeviceRequest
import io.github.iamnicknack.pjs.model.device.DeviceRegistry

class GrpcPortConfigService(
    private val deviceRegistry: DeviceRegistry
) : PortConfigServiceGrpcKt.PortConfigServiceCoroutineImplBase() {

    override suspend fun create(request: PortConfigPayload): PortConfigPayload {
        deviceRegistry.cannotContain(request.deviceId)
        val device = deviceRegistry.create(request.asGpioPortConfig())
        return (device.config as GpioPortConfig).asPortConfigPayload()
    }

    override suspend fun fetchConfig(request: DeviceRequest): PortConfigPayload {
        val device = deviceRegistry.deviceOrThrow<GpioPort>(request.deviceId)
        return (device.config as GpioPortConfig).asPortConfigPayload()
    }

    override suspend fun fetchDevices(request: Empty): PortConfigListResponse {
        return deviceRegistry
            .mapNotNull { it.config as? GpioPortConfig }
            .fold(PortConfigListResponse.newBuilder()) { builder, config ->
                builder.addConfig(config.asPortConfigPayload())
            }
            .build()
    }

    override suspend fun remove(request: DeviceRequest): Empty {
        val device = deviceRegistry.deviceOrThrow<GpioPort>(request.deviceId)
        deviceRegistry.remove(device)
        return Empty.getDefaultInstance()
    }
}