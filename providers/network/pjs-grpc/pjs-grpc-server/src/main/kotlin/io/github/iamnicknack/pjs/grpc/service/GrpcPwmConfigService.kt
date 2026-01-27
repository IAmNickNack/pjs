package io.github.iamnicknack.pjs.grpc.service

import io.github.iamnicknack.pjs.device.pwm.Pwm
import io.github.iamnicknack.pjs.device.pwm.PwmConfig
import io.github.iamnicknack.pjs.grpc.asPwmConfig
import io.github.iamnicknack.pjs.grpc.asPwmConfigPayload
import io.github.iamnicknack.pjs.grpc.cannotContain
import io.github.iamnicknack.pjs.grpc.deviceOrThrow
import io.github.iamnicknack.pjs.grpc.gen.v1.pwm.PwmConfigListResponse
import io.github.iamnicknack.pjs.grpc.gen.v1.pwm.PwmConfigPayload
import io.github.iamnicknack.pjs.grpc.gen.v1.pwm.PwmConfigServiceGrpcKt
import io.github.iamnicknack.pjs.grpc.gen.v1.types.DeviceRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.types.Empty
import io.github.iamnicknack.pjs.model.device.DeviceRegistry

class GrpcPwmConfigService(
    private val deviceRegistry: DeviceRegistry
) : PwmConfigServiceGrpcKt.PwmConfigServiceCoroutineImplBase() {

    override suspend fun create(request: PwmConfigPayload): PwmConfigPayload {
        deviceRegistry.cannotContain(request.deviceId)
        val device = deviceRegistry.create(request.asPwmConfig())
        return (device.config as PwmConfig).asPwmConfigPayload()
    }

    override suspend fun fetchConfig(request: DeviceRequest): PwmConfigPayload {
        val device = deviceRegistry.deviceOrThrow<Pwm>(request.deviceId)
        return (device.config as PwmConfig).asPwmConfigPayload()
    }

    override suspend fun fetchDevices(request: Empty): PwmConfigListResponse {
        return deviceRegistry
            .mapNotNull { it.config as? PwmConfig }
            .fold(PwmConfigListResponse.newBuilder()) { builder, config ->
                builder.addConfig(config.asPwmConfigPayload())
            }
            .build()
    }

    override suspend fun remove(request: DeviceRequest): Empty {
        val device = deviceRegistry.deviceOrThrow<Pwm>(request.deviceId)
        deviceRegistry.remove(device)
        return Empty.getDefaultInstance()
    }
}