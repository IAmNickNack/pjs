package io.github.iamnicknack.pjs.grpc.service

import io.github.iamnicknack.pjs.device.i2c.I2C
import io.github.iamnicknack.pjs.device.i2c.I2CConfig
import io.github.iamnicknack.pjs.grpc.asI2CBusConfig
import io.github.iamnicknack.pjs.grpc.asI2CBusConfigPayload
import io.github.iamnicknack.pjs.grpc.cannotContain
import io.github.iamnicknack.pjs.grpc.deviceOrThrow
import io.github.iamnicknack.pjs.grpc.gen.v1.i2c.bus.I2CBusConfigListResponse
import io.github.iamnicknack.pjs.grpc.gen.v1.i2c.bus.I2CBusConfigPayload
import io.github.iamnicknack.pjs.grpc.gen.v1.i2c.bus.I2CBusConfigServiceGrpcKt
import io.github.iamnicknack.pjs.grpc.gen.v1.types.DeviceRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.types.Empty
import io.github.iamnicknack.pjs.model.device.DeviceRegistry

class GrpcI2CBusConfigService(
    private val deviceRegistry: DeviceRegistry
) : I2CBusConfigServiceGrpcKt.I2CBusConfigServiceCoroutineImplBase() {

    override suspend fun create(request: I2CBusConfigPayload): I2CBusConfigPayload {
        deviceRegistry.cannotContain(request.deviceId)
        val device = deviceRegistry.create(request.asI2CBusConfig())
        return (device.config as I2CConfig).asI2CBusConfigPayload()
    }

    override suspend fun fetchConfig(request: DeviceRequest): I2CBusConfigPayload {
        val device = deviceRegistry.deviceOrThrow<I2C>(request.deviceId)
        return (device.config as I2CConfig).asI2CBusConfigPayload()
    }

    override suspend fun fetchDevices(request: Empty): I2CBusConfigListResponse {
        return deviceRegistry
            .mapNotNull { it.config as? I2CConfig }
            .fold(I2CBusConfigListResponse.newBuilder()) { builder, config ->
                builder.addConfig(config.asI2CBusConfigPayload())
            }
            .build()
    }

    override suspend fun remove(request: DeviceRequest): Empty {
        val device = deviceRegistry.deviceOrThrow<I2C>(request.deviceId)
        deviceRegistry.remove(device)
        return Empty.getDefaultInstance()
    }
}