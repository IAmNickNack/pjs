package io.github.iamnicknack.pjs.krpc.service

import io.github.iamnicknack.pjs.device.pwm.Pwm
import io.github.iamnicknack.pjs.device.pwm.PwmConfig
import io.github.iamnicknack.pjs.grpc.gen.v1.pwm.PwmConfigListResponse
import io.github.iamnicknack.pjs.grpc.gen.v1.pwm.PwmConfigPayload
import io.github.iamnicknack.pjs.grpc.gen.v1.pwm.PwmConfigService
import io.github.iamnicknack.pjs.grpc.gen.v1.pwm.invoke
import io.github.iamnicknack.pjs.grpc.gen.v1.types.DeviceRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.types.Empty
import io.github.iamnicknack.pjs.grpc.gen.v1.types.invoke
import io.github.iamnicknack.pjs.krpc.proto.asPwmConfig
import io.github.iamnicknack.pjs.krpc.proto.asPwmConfigPayload
import io.github.iamnicknack.pjs.krpc.proto.cannotContain
import io.github.iamnicknack.pjs.krpc.proto.deviceOrThrow
import io.github.iamnicknack.pjs.model.device.DeviceRegistry

class PwmConfigServiceImpl(
    private val deviceRegistry: DeviceRegistry
) : PwmConfigService {

    override suspend fun create(message: PwmConfigPayload): PwmConfigPayload {
        deviceRegistry.cannotContain(message.deviceId)
        val device = deviceRegistry.create(message.asPwmConfig())
        return (device.config as PwmConfig).asPwmConfigPayload()
    }

    override suspend fun fetchConfig(message: DeviceRequest): PwmConfigPayload {
        val device = deviceRegistry.deviceOrThrow<Pwm>(message.deviceId)
        return (device.config as PwmConfig).asPwmConfigPayload()
    }

    override suspend fun fetchDevices(message: Empty): PwmConfigListResponse {
        return deviceRegistry
            .mapNotNull { (it.config as? PwmConfig)?.asPwmConfigPayload() }
            .toList()
            .let { PwmConfigListResponse { config = it } }
    }

    override suspend fun remove(message: DeviceRequest): Empty {
        val device = deviceRegistry.deviceOrThrow<Pwm>(message.deviceId)
        device.close()
        return Empty {}
    }
}