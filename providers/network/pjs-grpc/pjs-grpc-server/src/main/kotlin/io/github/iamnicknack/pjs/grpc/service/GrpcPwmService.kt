package io.github.iamnicknack.pjs.grpc.service

import io.github.iamnicknack.pjs.device.pwm.Pwm
import io.github.iamnicknack.pjs.grpc.asIntegerResponse
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

    override suspend fun setFrequency(request: IntegerRequest): IntegerResponse {
        deviceRegistry.deviceOrThrow<Pwm>(request.deviceId).setFrequency(request.value)
        return request.asIntegerResponse()
    }

    override suspend fun getFrequency(request: DeviceRequest): IntegerResponse {
        return deviceRegistry.deviceOrThrow<Pwm>(request.deviceId).getFrequency().asIntegerResponse()
    }

    override suspend fun setDutyCycle(request: IntegerRequest): IntegerResponse {
        deviceRegistry.deviceOrThrow<Pwm>(request.deviceId).setDutyCycle(request.value)
        return request.asIntegerResponse()
    }

    override suspend fun getDutyCycle(request: DeviceRequest): IntegerResponse {
        return deviceRegistry.deviceOrThrow<Pwm>(request.deviceId).getDutyCycle().asIntegerResponse()
    }

    override suspend fun setPolarity(request: PolarityRequest): PolarityResponse {
        deviceRegistry.deviceOrThrow<Pwm>(request.deviceId).polarity = request.polarity.asPolarity()
        return request.asPolarityResponse()
    }

    override suspend fun getPolarity(request: DeviceRequest): PolarityResponse {
        return deviceRegistry.deviceOrThrow<Pwm>(request.deviceId).polarity.asPwmPolarity().asPolarityResponse()
    }
}