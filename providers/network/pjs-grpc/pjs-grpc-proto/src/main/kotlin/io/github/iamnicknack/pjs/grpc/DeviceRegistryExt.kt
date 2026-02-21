package io.github.iamnicknack.pjs.grpc

import io.github.iamnicknack.pjs.device.gpio.GpioEventMode
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig
import io.github.iamnicknack.pjs.device.gpio.GpioPortMode
import io.github.iamnicknack.pjs.device.i2c.I2CConfig
import io.github.iamnicknack.pjs.device.pwm.PwmConfig
import io.github.iamnicknack.pjs.device.spi.SpiConfig
import io.github.iamnicknack.pjs.grpc.gen.v1.i2c.bus.I2CBusConfigPayload
import io.github.iamnicknack.pjs.grpc.gen.v1.port.EventMode
import io.github.iamnicknack.pjs.grpc.gen.v1.port.PortConfigPayload
import io.github.iamnicknack.pjs.grpc.gen.v1.port.PortMode
import io.github.iamnicknack.pjs.grpc.gen.v1.pwm.PwmConfigPayload
import io.github.iamnicknack.pjs.grpc.gen.v1.spi.SpiConfigPayload
import io.github.iamnicknack.pjs.model.device.Device
import io.github.iamnicknack.pjs.model.device.DeviceRegistry
import io.grpc.Status

inline fun <reified T : Device<T>> DeviceRegistry.deviceOrThrow(deviceId: String): T =
    this.device(deviceId, T::class.java)
        ?: throw Status.NOT_FOUND
            .withDescription("Device with id $deviceId does not exist")
            .asRuntimeException()
            .also { it.printStackTrace(System.err) }

inline fun <reified T : Device<T>> DeviceRegistry.device(deviceId: String): T? =
    this.device(deviceId, T::class.java)

fun DeviceRegistry.cannotContain(deviceId: String) {
    if (this.contains(deviceId)) {
        throw Status.ALREADY_EXISTS
            .withDescription("Device with id $deviceId already exists")
            .asRuntimeException()
            .also { it.printStackTrace(System.err) }
    }
}

fun PortConfigPayload.asGpioPortConfig(): GpioPortConfig {
    return GpioPortConfig.builder()
        .id(this.deviceId)
        .pin(*this.pinNumberList.toIntArray())
        .portMode(this.portMode.asGpioPortMode())
        .eventMode(this.eventMode.asGpioEventMode())
        .defaultValue(this.defaultValue)
        .debounceDelay(this.debounceDelay)
        .build()
}

fun GpioPortConfig.asPortConfigPayload(): PortConfigPayload {
    return PortConfigPayload.newBuilder()
        .setDeviceId(this.id)
        .setPortMode(this.portMode.asPortMode())
        .setEventMode(this.eventMode.asEventMode())
        .setDefaultValue(this.defaultValue)
        .addAllPinNumber(this.pinNumber.toList())
        .setDebounceDelay(this.debounceDelay)
        .build()
}

fun PortMode.asGpioPortMode(): GpioPortMode {
    return when (this) {
        PortMode.INPUT -> GpioPortMode.INPUT
        PortMode.INPUT_PULLUP -> GpioPortMode.INPUT_PULLUP
        PortMode.INPUT_PULDOWN -> GpioPortMode.INPUT_PULLDOWN
        PortMode.OUTPUT -> GpioPortMode.OUTPUT
        PortMode.OUTPUT_OPENDRAIN -> GpioPortMode.OUTPUT_OPENDRAIN
        PortMode.OUTPUT_OPENSOURCE -> GpioPortMode.OUTPUT_OPENSOURCE
        else -> throw IllegalArgumentException("Unsupported port portMode: $this")
    }
}

fun GpioPortMode.asPortMode(): PortMode {
    return when(this) {
        GpioPortMode.INPUT -> PortMode.INPUT
        GpioPortMode.INPUT_PULLUP -> PortMode.INPUT_PULLUP
        GpioPortMode.INPUT_PULLDOWN -> PortMode.INPUT_PULDOWN
        GpioPortMode.OUTPUT -> PortMode.OUTPUT
        GpioPortMode.OUTPUT_OPENDRAIN -> PortMode.OUTPUT_OPENDRAIN
        GpioPortMode.OUTPUT_OPENSOURCE -> PortMode.OUTPUT_OPENSOURCE
    }
}

fun EventMode.asGpioEventMode(): GpioEventMode {
    return when (this) {
        EventMode.RISING -> GpioEventMode.RISING
        EventMode.FALLING -> GpioEventMode.FALLING
        EventMode.BOTH -> GpioEventMode.BOTH
        else -> GpioEventMode.NONE
    }
}

fun GpioEventMode.asEventMode(): EventMode {
    return when (this) {
        GpioEventMode.RISING -> EventMode.RISING
        GpioEventMode.FALLING -> EventMode.FALLING
        GpioEventMode.BOTH -> EventMode.BOTH
        else -> EventMode.NONE
    }
}

fun PwmConfig.asPwmConfigPayload(): PwmConfigPayload {
    return PwmConfigPayload.newBuilder()
        .setDeviceId(this.id)
        .setChip(this.chip)
        .setChannel(this.channel)
        .setDutyCycle(this.dutyCycle)
        .setPeriod(this.period)
        .build()
}

fun PwmConfigPayload.asPwmConfig(): PwmConfig {
    return PwmConfig.builder()
        .id(this.deviceId)
        .chip(this.chip)
        .channel(this.channel)
        .dutyCycle(this.dutyCycle)
        .period(this.period)
        .build()
}

fun I2CConfig.asI2CBusConfigPayload(): I2CBusConfigPayload {
    return I2CBusConfigPayload.newBuilder()
        .setDeviceId(id)
        .setBus(bus)
        .build()
}

fun I2CBusConfigPayload.asI2CBusConfig(): I2CConfig {
    return I2CConfig.builder()
        .id(deviceId)
        .bus(bus)
        .build()
}

fun SpiConfig.asSpiConfigPayload(): SpiConfigPayload {
    return SpiConfigPayload.newBuilder()
        .setDeviceId(id)
        .setBaudRate(baudRate)
        .setBus(bus)
        .setChipSelect(chipSelect)
        .setMode(mode)
        .setBitsPerWord(bitsPerWord)
        .setLsbFirst(lsbFirst)
        .build()
}

fun SpiConfigPayload.asSpiConfig(): SpiConfig {
    return SpiConfig.builder()
        .id(deviceId)
        .baudRate(baudRate)
        .bus(bus)
        .chipSelect(chipSelect)
        .mode(mode)
        .bitsPerWord(bitsPerWord)
        .lsbFirst(lsbFirst)
        .build()
}