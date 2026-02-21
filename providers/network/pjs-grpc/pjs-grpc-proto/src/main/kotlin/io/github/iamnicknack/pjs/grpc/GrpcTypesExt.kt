package io.github.iamnicknack.pjs.grpc

import com.google.protobuf.ByteString
import io.github.iamnicknack.pjs.device.pwm.Pwm
import io.github.iamnicknack.pjs.grpc.gen.v1.pwm.PolarityRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.pwm.PolarityResponse
import io.github.iamnicknack.pjs.grpc.gen.v1.pwm.PwmPolarity
import io.github.iamnicknack.pjs.grpc.gen.v1.types.DataRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.types.DataResponse
import io.github.iamnicknack.pjs.grpc.gen.v1.types.DeviceRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.types.IntegerRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.types.IntegerResponse
import io.github.iamnicknack.pjs.grpc.gen.v1.types.LongRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.types.LongResponse
import io.github.iamnicknack.pjs.model.device.DeviceConfig

fun DeviceConfig<*>.asDeviceRequest(): DeviceRequest = DeviceRequest.newBuilder()
    .setDeviceId(this.id)
    .build()

fun DeviceConfig<*>.asLongRequest(value: Long): LongRequest = LongRequest.newBuilder()
    .setDeviceId(this.id)
    .setValue(value)
    .build()

fun LongRequest.asLongResponse(): LongResponse = LongResponse.newBuilder()
    .setValue(this.value)
    .build()

fun DeviceConfig<*>.asIntegerRequest(value: Int): IntegerRequest = IntegerRequest.newBuilder()
    .setDeviceId(this.id)
    .setValue(value)
    .build()

fun IntegerRequest.asIntegerResponse(): IntegerResponse = IntegerResponse.newBuilder()
    .setValue(this.value)
    .build()

fun DeviceConfig<Pwm>.asPolarityRequest(polarity: PwmPolarity): PolarityRequest = PolarityRequest.newBuilder()
    .setDeviceId(this.id)
    .setPolarity(polarity)
    .build()

fun PwmPolarity.asPolarity(): Pwm.Polarity = when (this) {
    PwmPolarity.NORMAL -> Pwm.Polarity.NORMAL
    PwmPolarity.INVERTED -> Pwm.Polarity.INVERTED
    else -> throw IllegalArgumentException("Unsupported polarity: $this")
}

fun Pwm.Polarity.asPwmPolarity(): PwmPolarity = when (this) {
    Pwm.Polarity.NORMAL -> PwmPolarity.NORMAL
    Pwm.Polarity.INVERTED -> PwmPolarity.INVERTED
}

fun PolarityRequest.asPolarityResponse(): PolarityResponse = PolarityResponse.newBuilder()
    .setPolarity(this.polarity)
    .build()

fun PwmPolarity.asPolarityResponse(): PolarityResponse = PolarityResponse.newBuilder()
    .setPolarity(this)
    .build()

fun Long.asLongResponse(): LongResponse = LongResponse.newBuilder()
    .setValue(this)
    .build()

fun Int.asIntegerResponse(): IntegerResponse = IntegerResponse.newBuilder()
    .setValue(this)
    .build()

fun DeviceConfig<*>.asDataRequest(bytes: ByteArray): DataRequest = DataRequest.newBuilder()
    .setDeviceId(this.id)
    .setPayload(ByteString.copyFrom(bytes))
    .build()

fun DeviceConfig<*>.asDataRequest(bytes: ByteArray, offset: Int, length: Int): DataRequest = DataRequest.newBuilder()
    .setDeviceId(this.id)
    .setPayload(ByteString.copyFrom(bytes, offset, length))
    .build()

fun ByteArray.asDataResponse(): DataResponse = DataResponse.newBuilder()
    .setPayload(ByteString.copyFrom(this))
    .build()
