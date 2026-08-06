package io.github.iamnicknack.pjs.sandbox.registry

import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig
import io.github.iamnicknack.pjs.device.i2c.I2CConfig
import io.github.iamnicknack.pjs.device.pwm.PwmConfig
import io.github.iamnicknack.pjs.device.spi.SpiConfig
import io.github.iamnicknack.pjs.model.device.Device
import io.github.iamnicknack.pjs.model.device.DeviceConfig
import io.github.iamnicknack.pjs.model.device.DeviceRegistry
import io.github.iamnicknack.pjs.sandbox.registry.hardware.BasicHardwareAllocationIndex
import io.github.iamnicknack.pjs.sandbox.registry.hardware.HardwareAllocation
import io.github.iamnicknack.pjs.sandbox.registry.hardware.HardwareAllocationIndex
import io.github.iamnicknack.pjs.sandbox.registry.hardware.HardwareAllocationIndex.LineType
import io.github.iamnicknack.pjs.sandbox.registry.hardware.MutableHardwareAllocationIndex
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.function.Consumer

/**
 * A [DeviceRegistry] able to validate device configurations based on hardware availability.
 * @param delegate The delegate [DeviceRegistry] to use for device configuration creation.
 * @param availableHardware The [HardwareAllocationIndex] representing the available hardware.
 */
class HardwareAllocationDeviceRegistry(
    private val delegate: DeviceRegistry,
    private val availableHardware: HardwareAllocationIndex = BasicHardwareAllocationIndex.fromPinctrlOutput(readPinctrlOutput())
) : DeviceRegistry by delegate {

    /**
     * The index representing the hardware which has been allocated to devices in this registry.
     */
    private val usedHardware: MutableHardwareAllocationIndex<LineType> = MutableHardwareAllocationIndex {
        it.lineType
    }

    /**
     * Factory for validating [GpioPortConfig] prior to constructing a port.
     */
    private val gpioConfigLineFactory = GpioConfigLineFactory()

    /**
     * Factory for validating [I2CConfig] prior to constructing an i2c instance.
     */
    private val i2cConfigLineFactory = I2CConfigLineFactory()

    /**
     * Factory for validating [SpiConfig] prior to constructing an SPI instance.
     */
    private val spiConfigLineFactory = SpiConfigLineFactory()

    /**
     * Factory for validating [PwmConfig] prior to constructing a PWM instance.
     */
    private val pwmConfigLineFactory = PwmConfigLineFactory()

    /**
     * Validate that it is possible to allocate hardware before creating a device.
     */
    override fun <T : Device<T>, V : DeviceConfig<T>> create(config: V): T {
        val line = when(config) {
            is GpioPortConfig -> gpioConfigLineFactory.createLine(config)
            is I2CConfig -> i2cConfigLineFactory.createLine(config)
            is SpiConfig -> spiConfigLineFactory.createLine(config)
            is PwmConfig -> pwmConfigLineFactory.createLine(config)
            else -> throw IllegalArgumentException("Unsupported device configuration type: ${config::class.simpleName}")
        }
        return delegate.create(config).also { usedHardware.add(line) }
    }

    /**
     * Factory capable of converting a device configuration into a hardware allocation index line.
     */
    private interface ConfigLineFactory<T : DeviceConfig<*>> {
        /**
         * Create a hardware allocation index line from the given device configuration.
         * @param config The device configuration to convert.
         * @return The hardware allocation index line.
         */
        fun createLine(config: T): HardwareAllocationIndex.Line
    }

    /**
     * Factory to convert a GPIO device configuration into a hardware allocation index line.
     */
    private inner class GpioConfigLineFactory : ConfigLineFactory<GpioPortConfig> {

        private val gpioIndex = availableHardware.indexForType(LineType.GPIO)

        override fun createLine(config: GpioPortConfig): HardwareAllocationIndex.Line {
            val line = HardwareAllocationIndex.Line(
                LineType.GPIO,
                config.id,
                HardwareAllocation.fromOffsets(*config.pinNumber)
            )

            if (gpioIndex.findByMask(line.allocation.mask) == null) {
                throw PinsNotAvailableException(line, gpioIndex.remainder(line.allocation))
            }

            val inUse = usedHardware.findAllIntersectingByMask(line.allocation.mask)
            if (inUse.isNotEmpty()) {
                throw PinsInUseException(line, inUse)
            }

            return line
        }
    }

    /**
     * Factory to convert an I2C device configuration into a hardware allocation index line.
     */
    private inner class I2CConfigLineFactory : ConfigLineFactory<I2CConfig> {

        private val i2cIndex = availableHardware.indexForType(LineType.I2C)

        override fun createLine(config: I2CConfig): HardwareAllocationIndex.Line {
            val configured = i2cIndex.firstOrNull { it.bus == config.bus }

            if (configured == null) {
                throw BusNotConfiguredException(config.bus, LineType.I2C)
            }

            val inUse = usedHardware.findAllIntersectingByMask(configured.allocation.mask)
            if (inUse.isNotEmpty()) {
                throw BusInUseException(configured, inUse.first())
            }

            return HardwareAllocationIndex.Line(
                LineType.I2C,
                config.id,
                configured.allocation,
                config.bus
            )
        }
    }

    /**
     * Factory to convert an SPI device configuration into a hardware allocation index line.
     */
    private inner class SpiConfigLineFactory : ConfigLineFactory<SpiConfig> {
        private val spiIndex = availableHardware.indexForType(LineType.SPI)

        override fun createLine(config: SpiConfig): HardwareAllocationIndex.Line {
            val configured = spiIndex.firstOrNull { it.bus == config.bus }

            if (configured == null) {
                throw BusNotConfiguredException(config.bus, LineType.SPI)
            }

            val inUse = usedHardware.findAllIntersectingByMask(configured.allocation.mask)
            if (inUse.isNotEmpty()) {
                throw BusInUseException(configured, inUse.first())
            }

            return HardwareAllocationIndex.Line(
                LineType.SPI,
                config.id,
                configured.allocation,
                config.bus
            )
        }
    }

    /**
     * Factory to convert PWM device configurations into a hardware allocation index line.
     */
    private inner class PwmConfigLineFactory : ConfigLineFactory<PwmConfig> {
        private val pwmIndex = availableHardware.indexForType(LineType.PWM)

        override fun createLine(config: PwmConfig): HardwareAllocationIndex.Line {
            val configured = pwmIndex.firstOrNull { it.bus == config.chip }

            if (configured == null) {
                throw BusNotConfiguredException(config.chip, LineType.PWM)
            }

            val inUse = usedHardware.findAllIntersectingByMask(configured.allocation.mask)
            if (inUse.isNotEmpty()) {
                throw BusInUseException(configured, inUse.first())
            }

            return HardwareAllocationIndex.Line(
                LineType.PWM,
                config.id,
                configured.allocation,
                config.chip
            )
        }
    }


    /**
     * Exception thrown when attempting to allocate pins that are not available.
     * @param requested the requested line
     * @param available the available lines
     */
    class PinsNotAvailableException(
        val requested: HardwareAllocationIndex.Line,
        val unavailable: HardwareAllocation
    ) : RuntimeException("Pins not available: ${requested.name}, unavailable: ${unavailable.offsets.joinToString(", ")}")

    /**
     * Exception thrown when attempting to allocate hardware that is already in use.
     */
    class PinsInUseException(val requested: HardwareAllocationIndex.Line, conflicts: Iterable<HardwareAllocationIndex.Line>)
        : RuntimeException("Pins in use: ${requested.name}, conflicts: ${conflicts.joinToString(", ") { it.name }}") {

        val conflicts: List<HardwareAllocationIndex.Line> = conflicts
            .map { HardwareAllocationIndex.Line(it.lineType, it.name, it.allocation and requested.allocation) }
    }

    /**
     * Exception thrown when attempting to allocate hardware on a bus that is already in use.
     */
    class BusInUseException(val requested: HardwareAllocationIndex.Line, val current: HardwareAllocationIndex.Line)
        : RuntimeException("Bus ${requested.bus} is already in use for ${requested.lineType} by ${current.name}")

    /**
     * Exception thrown when attempting to allocate hardware on a bus that is not configured.
     */
    class BusNotConfiguredException(val bus: Int, val lineType: LineType)
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

    override fun forEach(action: Consumer<in Device<*>>?) = delegate.forEach(action)
    override fun spliterator(): Spliterator<Device<*>?> = delegate.spliterator()
}