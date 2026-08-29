package io.github.iamnicknack.pjs.sandbox.registry

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig
import io.github.iamnicknack.pjs.device.i2c.I2CConfig
import io.github.iamnicknack.pjs.device.pwm.PwmConfig
import io.github.iamnicknack.pjs.device.spi.SpiConfig
import io.github.iamnicknack.pjs.mock.MockDeviceFactory
import io.github.iamnicknack.pjs.model.device.DeviceRegistry
import io.github.iamnicknack.pjs.sandbox.registry.hardware.HardwareAllocation
import io.github.iamnicknack.pjs.sandbox.registry.hardware.HardwareAllocationException
import io.github.iamnicknack.pjs.sandbox.registry.hardware.HardwareAllocationIndex.Line
import io.github.iamnicknack.pjs.sandbox.registry.hardware.HardwareAllocationIndex.LineType
import io.github.iamnicknack.pjs.sandbox.registry.hardware.MutableHardwareAllocationIndex
import io.github.iamnicknack.pjs.sandbox.registry.hardware.ReadonlyHardwareAllocationIndex
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertFailsWith

class HardwareAllocationDeviceFactoryTest {

    @Test
    fun `can create gpio`() {
        val availableHardware = ReadonlyHardwareAllocationIndex(
            Line(LineType.GPIO, "available-gpio", HardwareAllocation.fromOffsets(2, 3))
        )

        val registry = HardwareAllocationDeviceFactory(MockDeviceFactory(), availableHardware)
        val config = GpioPortConfig.builder()
            .id("test-gpio")
            .pin(2, 3)
            .build()

        val device = registry.create(config)
        assertThat(device).isNotNull()
    }

    @Test
    fun `can remove gpio`() {
        val availableHardware = MutableHardwareAllocationIndex(
            Line(LineType.GPIO, "available-gpio", HardwareAllocation.fromOffsets(2, 3, 4, 5))
        )

        val usedHardware = MutableHardwareAllocationIndex()

        val registry = HardwareAllocationDeviceFactory(MockDeviceFactory(), availableHardware, usedHardware)
            .asDeviceRegistry()

        val config1 = GpioPortConfig.builder()
            .id("test-gpio")
            .pin(2, 4)
            .build()

        val config2 = GpioPortConfig.builder()
            .id("other-gpio")
            .pin(3, 5)
            .build()

        registry.create(config2)
        assertThat(usedHardware.containsName(config2.id())).isTrue()
        assertThat(usedHardware.findByMask(config2.mask)).isNotNull()

        val device = registry.create(config1)
        assertThat(device).isNotNull()
        assertThat(usedHardware.containsName(config1.id())).isTrue()

        device.close()
        assertThat(registry.contains(device.config.id)).isFalse()
        assertThat(usedHardware.containsName(config1.id())).isFalse()
        assertThat(usedHardware.findByMask(config1.mask)).isNull()
        assertThat(usedHardware.findByMask(config2.mask)).isNotNull()

        val recreatedDevice = registry.create(config1)
        assertThat(recreatedDevice).isNotNull()
        assertThat(usedHardware.containsName(config1.id())).isTrue()
    }

    @Test
    fun `cannot create with invalid gpios`() {
        val availableHardware = MutableHardwareAllocationIndex(
            Line(LineType.GPIO, "gpio1", HardwareAllocation.fromOffsets(2)),
            Line(LineType.GPIO, "gpio2", HardwareAllocation.fromOffsets(4))
        )

        val registry = HardwareAllocationDeviceFactory(MockDeviceFactory(), availableHardware)

        val config = GpioPortConfig.builder()
            .id("test-gpio")
            .pin(2, 3, 4, 5)
            .build()

        val error = assertFailsWith(HardwareAllocationException.PinsNotAvailable::class) {
            registry.create(config)
        }

        assertThat(error.unavailable).isEqualTo(HardwareAllocation.fromOffsets(3, 5))
    }

    @Test
    fun `cannot create conflicting gpio`() {
        val availableHardware = MutableHardwareAllocationIndex(
            Line(LineType.GPIO, "available-gpio", HardwareAllocation.fromOffsets(2, 3, 4))
        )

        val registry = HardwareAllocationDeviceFactory(MockDeviceFactory(), availableHardware)
        val config1 = GpioPortConfig.builder()
            .id("valid-gpio")
            .pin(2, 3)
            .build()

        val device = registry.create(config1)
        assertThat(device).isNotNull()

        val config2 = GpioPortConfig.builder()
            .id("invalid-gpio")
            .pin(3, 4)
            .build()

        val error = assertFailsWith(HardwareAllocationException.PinsInUse::class) {
            registry.create(config2)
        }

        assertThat(error.conflicts)
            .containsExactly(
                Line(LineType.GPIO, "valid-gpio", HardwareAllocation.fromOffsets(3))
            )
    }

    @TestFactory
    fun `can create bus device`(): List<DynamicTest> {
        class Expectation(
            val line: Line,
            val factory: (DeviceRegistry) -> Any
        )

        return listOf(
            Expectation(
                Line(LineType.SPI, "spi0", HardwareAllocation.fromOffsets(12, 13), 1)
            ) { registry: DeviceRegistry -> registry.create(SpiConfig.builder().id("spi1").bus(1).build()) },
            Expectation(
                Line(LineType.I2C, "i2c3", HardwareAllocation.fromOffsets(14, 15), 3)
            ) { registry: DeviceRegistry -> registry.create(I2CConfig.builder().id("i2c3").bus(3).build()) },
            Expectation(
                Line(LineType.PWM, "pwm0", HardwareAllocation.fromOffsets(16), 1, 1)
            ) { registry: DeviceRegistry -> registry.create(PwmConfig.builder().id("pwm1").chip(1).channel(1).build()) }
        ).map { expectation ->
            DynamicTest.dynamicTest("can create ${expectation.line.lineType} device") {
                val availableHardware = MutableHardwareAllocationIndex(expectation.line)
                val registry = HardwareAllocationDeviceFactory(MockDeviceFactory(), availableHardware)
                    .asDeviceRegistry()
                val result = expectation.factory(registry)
                assertThat(result).isNotNull()
            }
        }
    }

    @TestFactory
    fun `can create bus device when already in use`(): List<DynamicTest> {
        class Expectation(
            val line: Line,
            val factory: (DeviceRegistry) -> Any
        )

        return listOf(
            Expectation(
                Line(LineType.SPI, "spi1", HardwareAllocation.fromOffsets(12, 13), 1)
            ) { registry: DeviceRegistry -> registry.create(SpiConfig.builder().id("spi1").bus(1).build()) },
            Expectation(
                Line(LineType.I2C, "i2c3", HardwareAllocation.fromOffsets(14, 15), 3)
            ) { registry: DeviceRegistry -> registry.create(I2CConfig.builder().id("i2c3").bus(3).build()) },
            Expectation(
                Line(LineType.PWM, "pwm1", HardwareAllocation.fromOffsets(16), 1, 0)
            ) { registry: DeviceRegistry -> registry.create(PwmConfig.builder().id("pwm1").chip(1).build()) }
        ).map { expectation ->
            DynamicTest.dynamicTest("can create ${expectation.line.lineType} device when already in use") {
                val availableHardware = MutableHardwareAllocationIndex(expectation.line)
                val registry = HardwareAllocationDeviceFactory(MockDeviceFactory(), availableHardware)
                    .asDeviceRegistry()
                val result = expectation.factory(registry)
                assertThat(result).isNotNull()

                val error = assertFailsWith(HardwareAllocationException.BusInUse::class) {
                    expectation.factory(registry)
                }

                println(error)
            }
        }
    }

    @TestFactory
    fun `cannot create bus device with invalid bus`(): List<DynamicTest> {
        class Expectation(
            val line: Line,
            val factory: (DeviceRegistry) -> Any
        )

        return listOf(
            Expectation(
                Line(LineType.SPI, "spi0", HardwareAllocation.fromOffsets(12, 13), 1)
            ) { registry: DeviceRegistry -> registry.create(SpiConfig.builder().bus(0).build()) },
            Expectation(
                Line(LineType.I2C, "i2c3", HardwareAllocation.fromOffsets(14, 15), 3)
            ) { registry: DeviceRegistry -> registry.create(I2CConfig.builder().bus(0).build()) },
            Expectation(
                Line(LineType.PWM, "pwm0", HardwareAllocation.fromOffsets(16), 1, channel = 0)
            ) { registry: DeviceRegistry -> registry.create(PwmConfig.builder().chip(0).build()) }
        ).map { expectation ->
            DynamicTest.dynamicTest("cannot create ${expectation.line.lineType} device with invalid bus") {
                val availableHardware = MutableHardwareAllocationIndex(expectation.line)
                val registry = HardwareAllocationDeviceFactory(MockDeviceFactory(), availableHardware)
                    .asDeviceRegistry()
                val error = assertFailsWith(HardwareAllocationException.BusNotConfigured::class) {
                    expectation.factory(registry)
                }
                assertThat(error.bus).isEqualTo(0)
                assertThat(error.lineType).isEqualTo(expectation.line.lineType)
            }
        }
    }
}
