package io.github.iamnicknack.pjs.krpc.service

import io.github.iamnicknack.pjs.krpc.gen.v1.config.DeviceConfigService
import io.github.iamnicknack.pjs.krpc.gen.v1.types.DeviceRequest
import io.github.iamnicknack.pjs.krpc.gen.v1.types.Empty
import io.github.iamnicknack.pjs.krpc.gen.v1.types.invoke
import io.github.iamnicknack.pjs.model.device.DeviceRegistry
import io.grpc.Status

class DeviceConfigServiceImpl(
    private val deviceRegistry: DeviceRegistry
) : DeviceConfigService {

    override suspend fun removeDevice(message: DeviceRequest): Empty {
        val device = deviceRegistry.find { it.config.id == message.deviceId } ?: throw Status.NOT_FOUND
            .withDescription("Device with id ${message.deviceId} does not exist")
            .asRuntimeException()

        device.close()
        return Empty {}
    }
}