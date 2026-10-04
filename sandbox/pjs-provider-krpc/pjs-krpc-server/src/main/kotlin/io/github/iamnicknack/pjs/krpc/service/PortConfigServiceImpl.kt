package io.github.iamnicknack.pjs.krpc.service

import io.github.iamnicknack.pjs.device.gpio.GpioPort
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig
import io.github.iamnicknack.pjs.grpc.gen.v1.port.PortConfigListResponse
import io.github.iamnicknack.pjs.grpc.gen.v1.port.PortConfigPayload
import io.github.iamnicknack.pjs.grpc.gen.v1.port.PortConfigService
import io.github.iamnicknack.pjs.grpc.gen.v1.port.invoke
import io.github.iamnicknack.pjs.grpc.gen.v1.types.DeviceRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.types.Empty
import io.github.iamnicknack.pjs.grpc.gen.v1.types.invoke
import io.github.iamnicknack.pjs.krpc.proto.asGpioPortConfig
import io.github.iamnicknack.pjs.krpc.proto.asPortConfigPayload
import io.github.iamnicknack.pjs.krpc.proto.cannotContain
import io.github.iamnicknack.pjs.krpc.proto.withDevice
import io.github.iamnicknack.pjs.model.device.DeviceRegistry

class PortConfigServiceImpl(
    private val deviceRegistry: DeviceRegistry
) : PortConfigService {
    override suspend fun create(message: PortConfigPayload): PortConfigPayload {
        deviceRegistry.cannotContain(message.deviceId)
        return (deviceRegistry.create(message.asGpioPortConfig()).config as GpioPortConfig).asPortConfigPayload()
    }

    override suspend fun fetchConfig(message: DeviceRequest): PortConfigPayload {
        return deviceRegistry.withDevice<GpioPort, PortConfigPayload>(message.deviceId) {
            (config as GpioPortConfig).asPortConfigPayload()
        }
    }

    override suspend fun fetchDevices(message: Empty): PortConfigListResponse {
        return deviceRegistry
            .mapNotNull { (it.config as? GpioPortConfig)?.asPortConfigPayload() }
            .toList()
            .let { PortConfigListResponse.invoke { config = it } }
    }

    override suspend fun remove(message: DeviceRequest): Empty {
        return deviceRegistry.withDevice<GpioPort, Empty>(message.deviceId) {
            this.close()
            Empty {}
        }
    }
}
