package io.github.iamnicknack.pjs.krpc

import io.github.iamnicknack.pjs.grpc.gen.v1.config.DeviceConfigService
import io.github.iamnicknack.pjs.grpc.gen.v1.types.DeviceRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.types.Empty
import io.github.iamnicknack.pjs.grpc.gen.v1.types.invoke
import io.github.iamnicknack.pjs.model.device.DeviceRegistry
import io.grpc.Status
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DeviceConfigServiceImpl(
    private val deviceRegistry: DeviceRegistry
) : DeviceConfigService {

    private val logger: Logger = LoggerFactory.getLogger(DeviceConfigServiceImpl::class.java)

    override suspend fun removeDevice(message: DeviceRequest): Empty {
        val device = deviceRegistry.find { it.config.id == message.deviceId } ?: throw Status.NOT_FOUND
            .withDescription("Device with id ${message.deviceId} does not exist")
            .asRuntimeException()

        return runCatching { device.close() }
            .fold(
                onSuccess = { Empty {} },
                onFailure = { e ->
                    logger.error("Failed to close device with id ${message.deviceId}", e)
                    throw Status.INTERNAL
                        .withDescription("Failed to close device with id ${message.deviceId}")
                        .withCause(e)
                        .asRuntimeException()
                }
            )
    }
}