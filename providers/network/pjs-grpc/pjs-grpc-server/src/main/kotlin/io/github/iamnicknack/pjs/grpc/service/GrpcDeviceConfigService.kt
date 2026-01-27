package io.github.iamnicknack.pjs.grpc.service

import io.github.iamnicknack.pjs.grpc.gen.v1.config.DeviceConfigServiceGrpcKt
import io.github.iamnicknack.pjs.grpc.gen.v1.config.Empty
import io.github.iamnicknack.pjs.grpc.gen.v1.types.DeviceRequest
import io.github.iamnicknack.pjs.model.device.DeviceRegistry
import io.grpc.Status
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class GrpcDeviceConfigService(
    private val deviceRegistry: DeviceRegistry
) : DeviceConfigServiceGrpcKt.DeviceConfigServiceCoroutineImplBase() {

    private val logger: Logger = LoggerFactory.getLogger(GrpcDeviceConfigService::class.java)

    override suspend fun removeDevice(request: DeviceRequest): Empty {
        if (!deviceRegistry.contains(request.deviceId)) {
            throw Status.NOT_FOUND
                .withDescription("Device with id ${request.deviceId} does not exist")
                .asRuntimeException()
        }

        try {
            deviceRegistry.remove(request.deviceId)
        } catch (e: Exception) {
            logger.error("Failed to remove device: {}", request.deviceId, e)
            throw Status.INTERNAL
                .withDescription("Failed to remove device with id ${request.deviceId}")
                .asRuntimeException()
        }

        return Empty.getDefaultInstance()
    }
}