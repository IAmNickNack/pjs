package io.github.iamnicknack.pjs.krpc.proto

import io.github.iamnicknack.pjs.device.gpio.GpioEventMode
import io.github.iamnicknack.pjs.device.gpio.GpioPinMask
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig
import io.github.iamnicknack.pjs.device.gpio.GpioPortMode
import io.github.iamnicknack.pjs.device.i2c.I2CConfig
import io.github.iamnicknack.pjs.device.pwm.PwmConfig
import io.github.iamnicknack.pjs.device.spi.SpiConfig
import io.github.iamnicknack.pjs.grpc.gen.v1.i2c.bus.I2CBusConfigPayload
import io.github.iamnicknack.pjs.grpc.gen.v1.i2c.bus.invoke
import io.github.iamnicknack.pjs.grpc.gen.v1.port.EventMode
import io.github.iamnicknack.pjs.grpc.gen.v1.port.PortConfigPayload
import io.github.iamnicknack.pjs.grpc.gen.v1.port.PortMode
import io.github.iamnicknack.pjs.grpc.gen.v1.port.invoke
import io.github.iamnicknack.pjs.grpc.gen.v1.pwm.PwmConfigPayload
import io.github.iamnicknack.pjs.grpc.gen.v1.pwm.invoke
import io.github.iamnicknack.pjs.grpc.gen.v1.spi.SpiConfigPayload
import io.github.iamnicknack.pjs.grpc.gen.v1.spi.invoke
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

/**
 * Executes the given block with the device of the specified type and ID, throwing an error if the device does not exist.
 * @param deviceId The ID of the device to execute the block with.
 * @param block The block to execute with the device.
 * @return The result of the block.
 */
inline fun <reified T : Device<T>, R> DeviceRegistry.withDevice(deviceId: String, block: T.() -> R): R {
    val device = this.device(deviceId, T::class.java)
        ?: throw Status.NOT_FOUND
            .withDescription("Device with id $deviceId does not exist")
            .asRuntimeException()
            .also { it.printStackTrace(System.err) }

    return device.block()
}

fun DeviceRegistry.cannotContain(deviceId: String) {
    if (this.contains(deviceId)) {
        throw Status.ALREADY_EXISTS
            .withDescription("Device with id $deviceId already exists")
            .asRuntimeException()
            .also { it.printStackTrace(System.err) }
    }
}

@Suppress("SpreadOperator")
fun PortConfigPayload.asGpioPortConfig(): GpioPortConfig = GpioPortConfig.builder()
    .id(this.deviceId)
    .pin(*this.pinNumber.toIntArray())
    .portMode(this.portMode.asGpioPortMode())
    .eventMode(this.eventMode.asGpioEventMode())
    .defaultValue(this.defaultValue)
    .debounceDelay(this.debounceDelay)
    .build()

fun GpioPortConfig.asPortConfigPayload(): PortConfigPayload = PortConfigPayload.invoke {
    deviceId = this@asPortConfigPayload.id()
    portMode = this@asPortConfigPayload.portMode.asPortMode()
    eventMode = this@asPortConfigPayload.eventMode.asEventMode()
    defaultValue = this@asPortConfigPayload.defaultValue
    pinNumber = GpioPinMask.offsets(this@asPortConfigPayload.mask).toList()
    debounceDelay = this@asPortConfigPayload.debounceDelay
}

fun PortMode.asGpioPortMode(): GpioPortMode = when (this) {
    PortMode.INPUT -> GpioPortMode.INPUT
    PortMode.INPUT_PULLUP -> GpioPortMode.INPUT_PULLUP
    PortMode.INPUT_PULDOWN -> GpioPortMode.INPUT_PULLDOWN
    PortMode.OUTPUT -> GpioPortMode.OUTPUT
    PortMode.OUTPUT_OPENDRAIN -> GpioPortMode.OUTPUT_OPENDRAIN
    PortMode.OUTPUT_OPENSOURCE -> GpioPortMode.OUTPUT_OPENSOURCE
    else -> throw IllegalArgumentException("Unsupported port portMode: $this")
}

fun GpioPortMode.asPortMode(): PortMode = when(this) {
    GpioPortMode.INPUT -> PortMode.INPUT
    GpioPortMode.INPUT_PULLUP -> PortMode.INPUT_PULLUP
    GpioPortMode.INPUT_PULLDOWN -> PortMode.INPUT_PULDOWN
    GpioPortMode.OUTPUT -> PortMode.OUTPUT
    GpioPortMode.OUTPUT_OPENDRAIN -> PortMode.OUTPUT_OPENDRAIN
    GpioPortMode.OUTPUT_OPENSOURCE -> PortMode.OUTPUT_OPENSOURCE
}

fun EventMode.asGpioEventMode(): GpioEventMode = when (this) {
    EventMode.RISING -> GpioEventMode.RISING
    EventMode.FALLING -> GpioEventMode.FALLING
    EventMode.BOTH -> GpioEventMode.BOTH
    else -> GpioEventMode.NONE
}

fun GpioEventMode.asEventMode(): EventMode = when (this) {
    GpioEventMode.RISING -> EventMode.RISING
    GpioEventMode.FALLING -> EventMode.FALLING
    GpioEventMode.BOTH -> EventMode.BOTH
    else -> EventMode.NONE
}

fun PwmConfig.asPwmConfigPayload(): PwmConfigPayload = PwmConfigPayload.invoke {
    deviceId = this@asPwmConfigPayload.id
    chip = this@asPwmConfigPayload.chip
    channel = this@asPwmConfigPayload.channel
    dutyCycle = this@asPwmConfigPayload.dutyCycle
    period = this@asPwmConfigPayload.period
}

fun PwmConfigPayload.asPwmConfig(): PwmConfig = PwmConfig.builder()
    .id(this.deviceId)
    .chip(this.chip)
    .channel(this.channel)
    .dutyCycle(this.dutyCycle)
    .period(this.period)
    .build()

fun I2CConfig.asI2CBusConfigPayload(): I2CBusConfigPayload = I2CBusConfigPayload.invoke {
    deviceId = this@asI2CBusConfigPayload.id
    bus = this@asI2CBusConfigPayload.bus
}

fun I2CBusConfigPayload.asI2CBusConfig(): I2CConfig = I2CConfig.builder()
    .id(deviceId)
    .bus(bus)
    .build()

fun SpiConfig.asSpiConfigPayload(): SpiConfigPayload = SpiConfigPayload.invoke {
    deviceId = this@asSpiConfigPayload.id
    baudRate = this@asSpiConfigPayload.baudRate
    bus = this@asSpiConfigPayload.bus
    chipSelect = this@asSpiConfigPayload.chipSelect
    mode = this@asSpiConfigPayload.mode
    bitsPerWord = this@asSpiConfigPayload.bitsPerWord
    lsbFirst = this@asSpiConfigPayload.lsbFirst
}

fun SpiConfigPayload.asSpiConfig(): SpiConfig = SpiConfig.builder()
    .id(deviceId)
    .baudRate(baudRate)
    .bus(bus)
    .chipSelect(chipSelect)
    .mode(mode)
    .bitsPerWord(bitsPerWord)
    .lsbFirst(lsbFirst)
    .build()
