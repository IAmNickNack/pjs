@file:Suppress("TooManyFunctions")

package io.github.iamnicknack.pjs.krpc

import io.github.iamnicknack.pjs.device.gpio.GpioPortMode
import io.github.iamnicknack.pjs.device.pwm.Pwm
import io.github.iamnicknack.pjs.grpc.gen.v1.port.PortModePayload
import io.github.iamnicknack.pjs.grpc.gen.v1.port.invoke
import io.github.iamnicknack.pjs.grpc.gen.v1.pwm.PolarityRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.pwm.PolarityResponse
import io.github.iamnicknack.pjs.grpc.gen.v1.pwm.PwmPolarity
import io.github.iamnicknack.pjs.grpc.gen.v1.pwm.invoke
import io.github.iamnicknack.pjs.grpc.gen.v1.types.DataRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.types.DataResponse
import io.github.iamnicknack.pjs.grpc.gen.v1.types.DeviceRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.types.IntegerRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.types.IntegerResponse
import io.github.iamnicknack.pjs.grpc.gen.v1.types.LongRequest
import io.github.iamnicknack.pjs.grpc.gen.v1.types.LongResponse
import io.github.iamnicknack.pjs.grpc.gen.v1.types.invoke
import io.github.iamnicknack.pjs.model.device.DeviceConfig
import kotlinx.io.bytestring.ByteString

fun DeviceConfig<*>.asDeviceRequest() = DeviceRequest {
    deviceId = id
}

fun DeviceConfig<*>.asLongRequest(value: Long) = LongRequest {
    deviceId = id
    this.value = value
}

fun LongRequest.asLongResponse() = LongResponse {
    value = this@asLongResponse.value
}

fun DeviceConfig<*>.asIntegerRequest(value: Int) = IntegerRequest {
    deviceId = id
    this.value = value
}

fun IntegerRequest.asIntegerResponse() = IntegerResponse {
    value = this@asIntegerResponse.value
}

fun DeviceConfig<Pwm>.asPolarityRequest(polarity: PwmPolarity) = PolarityRequest {
    this.deviceId = id
    this.polarity = polarity
}

fun PwmPolarity.asPolarity(): Pwm.Polarity = when (this) {
    PwmPolarity.NORMAL -> Pwm.Polarity.NORMAL
    PwmPolarity.INVERTED -> Pwm.Polarity.INVERTED
    else -> throw IllegalArgumentException("Unsupported polarity: $this")
}

fun Pwm.Polarity.asPwmPolarity(): PwmPolarity = when (this) {
    Pwm.Polarity.NORMAL -> PwmPolarity.NORMAL
    Pwm.Polarity.INVERTED -> PwmPolarity.INVERTED
}

fun PolarityRequest.asPolarityResponse() = PolarityResponse {
    this.polarity = this@asPolarityResponse.polarity
}

fun PwmPolarity.asPolarityResponse() = PolarityResponse {
    this.polarity = this@asPolarityResponse
}

fun Long.asLongResponse() = LongResponse {
    this.value = this@asLongResponse
}

fun Int.asIntegerResponse() = IntegerResponse {
    this.value = this@asIntegerResponse
}

fun DeviceConfig<*>.asDataRequest(bytes: ByteArray) = DataRequest {
    deviceId = this@asDataRequest.id
    payload = ByteString(bytes)
}

fun DeviceConfig<*>.asDataRequest(bytes: ByteArray, offset: Int, length: Int) = DataRequest {
    deviceId = this@asDataRequest.id
    payload = ByteString(bytes, offset, length)
}

fun ByteArray.asDataResponse() = DataResponse {
    payload = ByteString(this@asDataResponse)
}

fun DeviceConfig<*>.asPortModePayload(portMode: GpioPortMode) = PortModePayload {
    deviceId = this@asPortModePayload.id
    this.portMode = portMode.asPortMode()
}
