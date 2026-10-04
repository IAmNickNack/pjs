package io.github.iamnicknack.pjs.krpc.service

import io.github.iamnicknack.pjs.device.spi.Spi
import io.github.iamnicknack.pjs.device.spi.SpiConfig
import io.github.iamnicknack.pjs.krpc.asSpiConfig
import io.github.iamnicknack.pjs.krpc.asSpiConfigPayload
import io.github.iamnicknack.pjs.krpc.cannotContain
import io.github.iamnicknack.pjs.krpc.deviceOrThrow
import io.github.iamnicknack.pjs.grpc.gen.v1.spi.SpiConfigListResponse
import io.github.iamnicknack.pjs.grpc.gen.v1.spi.SpiConfigPayload
import io.github.iamnicknack.pjs.grpc.gen.v1.spi.SpiConfigService
import io.github.iamnicknack.pjs.grpc.gen.v1.spi.invoke
import io.github.iamnicknack.pjs.grpc.gen.v1.types.DeviceRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.types.Empty
import io.github.iamnicknack.pjs.grpc.gen.v1.types.invoke
import io.github.iamnicknack.pjs.model.device.DeviceRegistry

class SpiConfigServiceImpl(
    private val deviceRegistry: DeviceRegistry
) : SpiConfigService {

    override suspend fun create(message: SpiConfigPayload): SpiConfigPayload {
        deviceRegistry.cannotContain(message.deviceId)
        val device = deviceRegistry.create(message.asSpiConfig())
        return (device.config as SpiConfig).asSpiConfigPayload()
    }

    override suspend fun fetchConfig(message: DeviceRequest): SpiConfigPayload {
        val device = deviceRegistry.deviceOrThrow<Spi>(message.deviceId)
        return (device.config as SpiConfig).asSpiConfigPayload()
    }

    override suspend fun fetchDevices(message: Empty): SpiConfigListResponse {
        return deviceRegistry
            .mapNotNull { (it.config as? SpiConfig)?.asSpiConfigPayload() }
            .toList()
            .let {
                SpiConfigListResponse.invoke {
                    config = it
                }
            }
    }

    override suspend fun remove(message: DeviceRequest): Empty {
        val device = deviceRegistry.deviceOrThrow<Spi>(message.deviceId)
        device.close()
        return Empty {}
    }
}