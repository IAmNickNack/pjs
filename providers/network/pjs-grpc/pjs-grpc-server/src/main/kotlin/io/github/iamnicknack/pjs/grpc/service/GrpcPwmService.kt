package io.github.iamnicknack.pjs.grpc.service

import io.github.iamnicknack.pjs.device.pwm.Pwm
import io.github.iamnicknack.pjs.grpc.asIntegerResponse
import io.github.iamnicknack.pjs.grpc.asLongResponse
import io.github.iamnicknack.pjs.grpc.asPolarity
import io.github.iamnicknack.pjs.grpc.asPolarityResponse
import io.github.iamnicknack.pjs.grpc.asPwmPolarity
import io.github.iamnicknack.pjs.grpc.deviceOrThrow
import io.github.iamnicknack.pjs.grpc.gen.v1.pwm.PolarityRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.pwm.PolarityResponse
import io.github.iamnicknack.pjs.grpc.gen.v1.pwm.PwmServiceGrpcKt
import io.github.iamnicknack.pjs.grpc.gen.v1.types.DeviceRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.types.Empty
import io.github.iamnicknack.pjs.grpc.gen.v1.types.IntegerRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.types.IntegerResponse
import io.github.iamnicknack.pjs.grpc.gen.v1.types.LongRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.types.LongResponse
import io.github.iamnicknack.pjs.model.device.DeviceRegistry

class GrpcPwmService(
    private val deviceRegistry: DeviceRegistry
) : PwmServiceGrpcKt.PwmServiceCoroutineImplBase() {

    override suspend fun on(request: DeviceRequest): Empty {
        deviceRegistry.deviceOrThrow<Pwm>(request.deviceId).on()
        return Empty.getDefaultInstance()
    }

    override suspend fun off(request: DeviceRequest): Empty {
        deviceRegistry.deviceOrThrow<Pwm>(request.deviceId).off()
        return Empty.getDefaultInstance()
    }

    override suspend fun setPeriod(request: LongRequest): LongResponse {
        deviceRegistry.deviceOrThrow<Pwm>(request.deviceId).setPeriod(request.value)
        return request.asLongResponse()
    }

    override suspend fun getPeriod(request: DeviceRequest): LongResponse {
        return deviceRegistry.deviceOrThrow<Pwm>(request.deviceId).getPeriod().asLongResponse()
    }

    override suspend fun setDutyCycle(request: LongRequest): LongResponse {
        deviceRegistry.deviceOrThrow<Pwm>(request.deviceId).setDutyCycle(request.value)
        return request.asLongResponse()
    }

    override suspend fun getDutyCycle(request: DeviceRequest): LongResponse {
        return deviceRegistry.deviceOrThrow<Pwm>(request.deviceId).getDutyCycle().asLongResponse()
    }

    override suspend fun setPolarity(request: PolarityRequest): PolarityResponse {
        deviceRegistry.deviceOrThrow<Pwm>(request.deviceId).polarity = request.polarity.asPolarity()
        return request.asPolarityResponse()
    }

    override suspend fun getPolarity(request: DeviceRequest): PolarityResponse {
        return deviceRegistry.deviceOrThrow<Pwm>(request.deviceId).polarity.asPwmPolarity().asPolarityResponse()
    }
}