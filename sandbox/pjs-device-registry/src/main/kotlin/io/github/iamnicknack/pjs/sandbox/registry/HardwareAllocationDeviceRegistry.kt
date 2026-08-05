package io.github.iamnicknack.pjs.sandbox.registry

import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig
import io.github.iamnicknack.pjs.device.i2c.I2CConfig
import io.github.iamnicknack.pjs.model.device.Device
import io.github.iamnicknack.pjs.model.device.DeviceConfig
import io.github.iamnicknack.pjs.model.device.DeviceRegistry
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.function.Consumer

class HardwareAllocationDeviceRegistry(
    private val delegate: DeviceRegistry
) : DeviceRegistry by delegate {

    private val availableHardware: HardwareAllocationIndexImpl = HardwareAllocationIndexImpl.fromPinctrlOutput(readPinctrlOutput())
    private val usedHardware: MutableHardwareAllocationIndex<HardwareAllocationIndex.LineType> = MutableHardwareAllocationIndex {
        it.lineType
    }

    /**
     * Validate that it is possible to allocate hardware before creating a device.
     *
     */
    override fun <T : Device<T>, V : DeviceConfig<T>> create(config: V): T {
        val line = when(config) {
            is GpioPortConfig -> lineForGpioConfig(config)
            is I2CConfig -> lineForI2CConfig(config)
            else -> throw IllegalArgumentException("Unsupported device configuration type: ${config::class.simpleName}")
        }
        usedHardware.add(line)
        return delegate.create(config)
    }

    /**
     * Validate GPIO config
     */
    private fun lineForGpioConfig(config: GpioPortConfig): HardwareAllocationIndex.Line {
        // TODO: This wont work
        //  maybe add `flattenByType` or don't parse GPIOs to individual lines
        if (availableHardware.findAllByMask(config.pinMask.toLong()).isEmpty()) {
            throw IllegalArgumentException("Invalid GPIO configuration: ${config.pinMask}")
        }

        val requestedLine = HardwareAllocationIndex.Line(
            HardwareAllocationIndex.LineType.GPIO,
            config.id,
            HardwareAllocation.fromOffsets(*config.pinNumber)
        )
        val current = usedHardware.findAllByMask(config.pinMask.toLong())
        if (current.isNotEmpty()) {
            throw PinsInUseException(requestedLine, current)
        }
        return requestedLine
    }

    /**
     * Validate I2C config
     */
    private fun lineForI2CConfig(config: I2CConfig): HardwareAllocationIndex.Line {
        val configured = availableHardware.findAllByType(HardwareAllocationIndex.LineType.I2C)
            .firstOrNull { it.bus == config.bus }

        if (configured == null) {
            throw BusNotConfiguredException(config.bus, HardwareAllocationIndex.LineType.I2C)
        }

        val requestedLine = HardwareAllocationIndex.Line(
            HardwareAllocationIndex.LineType.I2C,
            config.id,
            configured.allocation,
            config.bus
        )

        return requestedLine
    }

    override fun forEach(action: Consumer<in Device<*>>?) = delegate.forEach(action)
    override fun spliterator(): Spliterator<Device<*>?> = delegate.spliterator()

    /**
     * Exception thrown when attempting to allocate hardware that is already in use.
     */
    class PinsInUseException(val requested: HardwareAllocationIndex.Line, conflicts: Iterable<HardwareAllocationIndex.Line>)
        : RuntimeException("Pins in use: ${requested.name}, conflicts: ${conflicts.joinToString(", ") { it.name }}") {

        val conflicts: List<HardwareAllocationIndex.Line> = conflicts
            .map { HardwareAllocationIndex.Line(it.lineType, it.name, it.allocation and requested.allocation) }
    }

    /**
     * Exception thrown when attempting to allocate hardware on a bus that is not configured.
     */
    class BusNotConfiguredException(val bus: Int, val lineType: HardwareAllocationIndex.LineType)
        : RuntimeException("Bus $bus is not configured for $lineType")

    companion object {
        /**
         * Reads the contents of the pinctrl-output.txt resource file.
         *
         * Used for testing
         */
        private fun readPinctrlOutput(): String {
            val input = javaClass.getResourceAsStream("/pinctrl-output.txt")
                ?: throw IOException("Missing test resource pinctrl-output.txt")

            return input.use { String(it.readAllBytes(), StandardCharsets.UTF_8) }
        }
    }
}