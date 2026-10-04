package io.github.iamnicknack.pjs.krpc.service

import io.github.iamnicknack.pjs.device.pwm.Pwm
import io.github.iamnicknack.pjs.grpc.gen.v1.pwm.PolarityRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.pwm.PolarityResponse
import io.github.iamnicknack.pjs.grpc.gen.v1.pwm.PwmService
import io.github.iamnicknack.pjs.grpc.gen.v1.types.DeviceRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.types.Empty
import io.github.iamnicknack.pjs.grpc.gen.v1.types.LongRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.types.LongResponse
import io.github.iamnicknack.pjs.grpc.gen.v1.types.invoke
import io.github.iamnicknack.pjs.krpc.proto.asLongResponse
import io.github.iamnicknack.pjs.krpc.proto.asPolarity
import io.github.iamnicknack.pjs.krpc.proto.asPolarityResponse
import io.github.iamnicknack.pjs.krpc.proto.asPwmPolarity
import io.github.iamnicknack.pjs.krpc.proto.deviceOrThrow
import io.github.iamnicknack.pjs.model.device.DeviceRegistry

class PwmServiceImpl(
    private val deviceRegistry: DeviceRegistry
) : PwmService {

    override suspend fun on(message: DeviceRequest): Empty {
        deviceRegistry.deviceOrThrow<Pwm>(message.deviceId).on()
        return Empty.invoke {}
    }

    override suspend fun off(message: DeviceRequest): Empty {
        deviceRegistry.deviceOrThrow<Pwm>(message.deviceId).off()
        return Empty {}
    }

    override suspend fun setPeriod(message: LongRequest): LongResponse {
        deviceRegistry.deviceOrThrow<Pwm>(message.deviceId).setPeriod(message.value)
        return message.asLongResponse()
    }

    override suspend fun getPeriod(message: DeviceRequest): LongResponse {
        return deviceRegistry.deviceOrThrow<Pwm>(message.deviceId).getPeriod().asLongResponse()
    }

    override suspend fun setDutyCycle(message: LongRequest): LongResponse {
        deviceRegistry.deviceOrThrow<Pwm>(message.deviceId).setDutyCycle(message.value)
        return message.asLongResponse()
    }

    override suspend fun getDutyCycle(message: DeviceRequest): LongResponse {
        return deviceRegistry.deviceOrThrow<Pwm>(message.deviceId).getDutyCycle().asLongResponse()
    }

    override suspend fun setPolarity(message: PolarityRequest): PolarityResponse {
        deviceRegistry.deviceOrThrow<Pwm>(message.deviceId).polarity = message.polarity.asPolarity()
        return message.asPolarityResponse()
    }

    override suspend fun getPolarity(message: DeviceRequest): PolarityResponse {
        return deviceRegistry.deviceOrThrow<Pwm>(message.deviceId).polarity.asPwmPolarity().asPolarityResponse()
    }
}