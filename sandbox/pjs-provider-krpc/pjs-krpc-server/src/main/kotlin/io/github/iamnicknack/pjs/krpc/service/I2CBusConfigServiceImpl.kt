package io.github.iamnicknack.pjs.krpc.service

import io.github.iamnicknack.pjs.device.i2c.I2C
import io.github.iamnicknack.pjs.device.i2c.I2CConfig
import io.github.iamnicknack.pjs.grpc.gen.v1.i2c.bus.I2CBusConfigListResponse
import io.github.iamnicknack.pjs.grpc.gen.v1.i2c.bus.I2CBusConfigPayload
import io.github.iamnicknack.pjs.grpc.gen.v1.i2c.bus.I2CBusConfigService
import io.github.iamnicknack.pjs.grpc.gen.v1.i2c.bus.invoke
import io.github.iamnicknack.pjs.grpc.gen.v1.types.DeviceRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.types.Empty
import io.github.iamnicknack.pjs.grpc.gen.v1.types.invoke
import io.github.iamnicknack.pjs.krpc.asI2CBusConfig
import io.github.iamnicknack.pjs.krpc.asI2CBusConfigPayload
import io.github.iamnicknack.pjs.krpc.cannotContain
import io.github.iamnicknack.pjs.krpc.withDevice
import io.github.iamnicknack.pjs.model.device.DeviceRegistry

class I2CBusConfigServiceImpl(
    private val deviceRegistry: DeviceRegistry
) : I2CBusConfigService {
    override suspend fun create(message: I2CBusConfigPayload): I2CBusConfigPayload {
        deviceRegistry.cannotContain(message.deviceId)
        return (deviceRegistry.create(message.asI2CBusConfig()).config as I2CConfig).asI2CBusConfigPayload()
    }

    override suspend fun fetchConfig(message: DeviceRequest): I2CBusConfigPayload {
        return deviceRegistry.withDevice<I2C, I2CBusConfigPayload>(message.deviceId) {
            (config as I2CConfig).asI2CBusConfigPayload()
        }
    }

    override suspend fun fetchDevices(message: Empty): I2CBusConfigListResponse {
        return deviceRegistry
            .mapNotNull { (it.config as? I2CConfig)?.asI2CBusConfigPayload() }
            .toList()
            .let { I2CBusConfigListResponse.invoke { config = it } }
    }

    override suspend fun remove(message: DeviceRequest): Empty {
        return deviceRegistry.withDevice<I2C, Empty>(message.deviceId) {
            this.close()
            Empty {}
        }
    }
}
