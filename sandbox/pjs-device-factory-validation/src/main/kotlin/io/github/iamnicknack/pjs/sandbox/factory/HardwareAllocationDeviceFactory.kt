package io.github.iamnicknack.pjs.sandbox.factory

import io.github.iamnicknack.pjs.device.gpio.GpioPinMask
import io.github.iamnicknack.pjs.device.gpio.GpioPort
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig
import io.github.iamnicknack.pjs.device.i2c.I2C
import io.github.iamnicknack.pjs.device.i2c.I2CConfig
import io.github.iamnicknack.pjs.device.pwm.Pwm
import io.github.iamnicknack.pjs.device.pwm.PwmConfig
import io.github.iamnicknack.pjs.device.spi.Spi
import io.github.iamnicknack.pjs.device.spi.SpiConfig
import io.github.iamnicknack.pjs.model.device.Device
import io.github.iamnicknack.pjs.model.device.DeviceConfig
import io.github.iamnicknack.pjs.model.device.DeviceRegistry
import io.github.iamnicknack.pjs.model.device.GenericDeviceFactory
import io.github.iamnicknack.pjs.sandbox.factory.hardware.HardwareAllocation
import io.github.iamnicknack.pjs.sandbox.factory.hardware.HardwareAllocationException
import io.github.iamnicknack.pjs.sandbox.factory.hardware.HardwareAllocationIndex
import io.github.iamnicknack.pjs.sandbox.factory.hardware.HardwareAllocationIndex.LineType
import io.github.iamnicknack.pjs.sandbox.factory.hardware.MutableHardwareAllocationIndex
import io.github.iamnicknack.pjs.sandbox.factory.hardware.ReadonlyHardwareAllocationIndex
import io.github.iamnicknack.pjs.sandbox.factory.line.LineSupplier
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.math.max

/**
 * A [DeviceRegistry] able to validate device configurations based on hardware availability.
 * @param delegate The delegate [DeviceRegistry] to use for device configuration creation.
 * @param availableHardware The [HardwareAllocationIndex] representing the available hardware.
 * @param usedHardware The index representing the hardware which has been allocated to devices in this registry.
 */
class HardwareAllocationDeviceFactory(
    private val delegate: GenericDeviceFactory,
    private val availableHardware: HardwareAllocationIndex,
    private val usedHardware: HardwareAllocationIndex.Mutable
) : GenericDeviceFactory {

    constructor(
        delegate: GenericDeviceFactory,
        availableHardware: HardwareAllocationIndex,
    ) : this(
        delegate,
        availableHardware,
        MutableHardwareAllocationIndex()
    )

    constructor(
        delegate: GenericDeviceFactory,
        lineSupplier: LineSupplier
    ) : this(
        delegate,
        ReadonlyHardwareAllocationIndex(lineSupplier.lines()),
        MutableHardwareAllocationIndex()
    )

    private val logger: Logger = LoggerFactory.getLogger(HardwareAllocationDeviceFactory::class.java)

    init {
        logger.info("Initializing HardwareAllocationDeviceFactory")
        availableHardware.dump { logger.info(it) }
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
    @Suppress("UNCHECKED_CAST")
    override fun <T : Device<T>, V : DeviceConfig<T>> create(config: V): T {
        val device = when(config) {
            is GpioPortConfig -> gpioConfigLineFactory.validateLine(config)
                .let { line ->
                    logger.atDebug().log { line.dumpString() }
                    GpioPortDelegate(delegate.create(config))
                        .also { usedHardware.add(line) }
                }
            is I2CConfig -> i2cConfigLineFactory.validateLine(config)
                .let { line ->
                    logger.atDebug().log { line.dumpString() }
                    I2CDelegate(delegate.create(config))
                        .also { usedHardware.add(line) }
                }
            is SpiConfig -> spiConfigLineFactory.validateLine(config)
                .let { line ->
                    logger.atDebug().log { line.dumpString() }
                    SpiDelegate(delegate.create(config))
                        .also { usedHardware.add(line) }
                }
            is PwmConfig -> pwmConfigLineFactory.validateLine(config)
                .let { line ->
                    logger.atDebug().log { line.dumpString() }
                    PwmDelegate(delegate.create(config))
                        .also { usedHardware.add(line) }
                }
            else -> throw IllegalArgumentException("Unsupported device configuration type: ${config::class.simpleName}")
        }

        return device as T
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

        /**
         * Validate the hardware allocation line for the given device configuration.
         * @param config The device configuration to validate.
         * @return The validated hardware allocation index line.
         */
        fun validateLine(config: T): HardwareAllocationIndex.Line = createLine(config)
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
                HardwareAllocation(config.mask)
            )

            if (gpioIndex.findByAllocation(line.allocation) == null) {
                throw HardwareAllocationException.PinsNotAvailable(line, gpioIndex.remainder(line.allocation))
            }

            return line
        }

        override fun validateLine(config: GpioPortConfig): HardwareAllocationIndex.Line {
            val line = createLine(config)

            val inUse = usedHardware.findAllIntersectingByAllocation(line.allocation)
            if (inUse.isNotEmpty()) {
                throw HardwareAllocationException.PinsInUse(line, inUse)
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
                throw HardwareAllocationException.BusNotConfigured(config.bus, LineType.I2C)
            }

            return HardwareAllocationIndex.Line(
                LineType.I2C,
                config.id,
                configured.allocation,
                config.bus
            )
        }

        override fun validateLine(config: I2CConfig): HardwareAllocationIndex.Line {
            val line = createLine(config)

            val inUse = usedHardware.findAllIntersectingByAllocation(line.allocation)
            if (inUse.isNotEmpty()) {
                throw HardwareAllocationException.BusInUse(line, inUse.first())
            }

            return line
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
                throw HardwareAllocationException.BusNotConfigured(config.bus, LineType.SPI)
            }

            return HardwareAllocationIndex.Line(
                LineType.SPI,
                config.id,
                configured.allocation,
                config.bus
            )
        }

        override fun validateLine(config: SpiConfig): HardwareAllocationIndex.Line {
            val line = createLine(config)

            val inUse = usedHardware.findAllIntersectingByAllocation(line.allocation)
            if (inUse.isNotEmpty()) {
                throw HardwareAllocationException.BusInUse(line, inUse.first())
            }

            return line
        }
    }

    /**
     * Factory to convert PWM device configurations into a hardware allocation index line.
     */
    private inner class PwmConfigLineFactory : ConfigLineFactory<PwmConfig> {
        private val pwmIndex = availableHardware.indexForType(LineType.PWM)

        override fun createLine(config: PwmConfig): HardwareAllocationIndex.Line {
            var found = pwmIndex.filter { it.bus == config.chip }
            if (found.isEmpty()) {
                throw HardwareAllocationException.BusNotConfigured(config.chip, LineType.PWM)
            }

            found = found.filter { it.channel == config.channel }
            if (found.isEmpty()) {
                throw HardwareAllocationException.ChannelNotConfigured(config.chip, config.channel, LineType.PWM)
            }

            return HardwareAllocationIndex.Line(
                LineType.PWM,
                config.id,
                found.first().allocation,
                config.chip,
                channel = config.channel
            )
        }

        override fun validateLine(config: PwmConfig): HardwareAllocationIndex.Line {
            val line = createLine(config)

            val inUse = usedHardware.findAllIntersectingByAllocation(line.allocation)
            if (inUse.isNotEmpty()) {
                throw HardwareAllocationException.BusInUse(line, inUse.first())
            }

            return line
        }
    }

    @Suppress("JavaDefaultMethodsNotOverriddenByDelegation")
    private inner class GpioPortDelegate(
        private val delegate: GpioPort
    ) : GpioPort by delegate {
        override fun close() {
            gpioConfigLineFactory.createLine(this.config as GpioPortConfig)
                .also(usedHardware::remove)
            delegate.close()
        }
    }

    @Suppress("JavaDefaultMethodsNotOverriddenByDelegation")
    private inner class PwmDelegate(
        private val delegate: Pwm
    ) : Pwm by delegate {
        override fun close() {
            pwmConfigLineFactory.createLine(this.config as PwmConfig)
                .also(usedHardware::remove)
            delegate.close()
        }
    }

    @Suppress("JavaDefaultMethodsNotOverriddenByDelegation")
    private inner class SpiDelegate(
        private val delegate: Spi
    ) : Spi by delegate {
        override fun close() {
            spiConfigLineFactory.createLine(this.config as SpiConfig)
                .also(usedHardware::remove)
            delegate.close()
        }
    }

    private inner class I2CDelegate(
        private val delegate: I2C
    ) : I2C by delegate {
        override fun close() {
            i2cConfigLineFactory.createLine(this.config as I2CConfig)
                .also(usedHardware::remove)
            delegate.close()
        }
    }

    companion object {

        private fun HardwareAllocationIndex.Line.dumpString(nameLength: Int = name.length): String {
            val pinMask = GpioPinMask.fromMask(allocation.mask and 0xFFFFFFFFL)
            return "> ${pinMask.maskString} " +
                    "- ${name.padEnd(nameLength + 1, ' ')}" +
                    ": ${pinMask.offsets().joinToString(", ") { "%02d".format(it) }}"
        }

        private fun HardwareAllocationIndex.dump(consumer: (String) -> Unit) {
            val maxNameLength = this.map { it.name.length }
                .fold(0) { acc, v -> max(acc, v) }

            forEach { line ->
                consumer(line.dumpString(maxNameLength))
            }
        }
    }
}
