package io.github.iamnicknack.pjs.sandbox.registry.hardware

import io.github.iamnicknack.pjs.sandbox.registry.hardware.HardwareAllocationIndex.LineType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ReadonlyHardwareAllocationIndexTest {

    private val lineSupplier = LineSupplier.fromPinctrlResource("/pinctrl-output.txt")

    @Test
    fun showAllLines() {
        val index = lineSupplier.lines()
        for (line in index) {
            println(line)
        }
    }

    @Test
    fun pinctrlParserParsesFromReader() {
        val index = ReadonlyHardwareAllocationIndex(lineSupplier.lines())
        assertExpectedLines(index)
    }

    @Test
    fun excludesOffsetsAbove63() {
        val index = ReadonlyHardwareAllocationIndex(lineSupplier.lines())
        assertTrue(index.flatMap { it.allocation.offsets }.all { it in 0..63 })
    }

    @Test
    fun gpioOnlyIncludesEntriesWithDashedCurrentValue() {
        val index = ReadonlyHardwareAllocationIndex(lineSupplier.lines())
        val gpio = assertNotNull(index.findByName("GPIO"))
        assertFalse(gpio.allocation.offsets.contains(7))
        assertFalse(gpio.allocation.offsets.contains(8))
        assertFalse(gpio.allocation.offsets.contains(28))
        assertFalse(gpio.allocation.offsets.contains(29))
        assertFalse(gpio.allocation.offsets.contains(32))
        assertFalse(gpio.allocation.offsets.contains(33))
        assertFalse(gpio.allocation.offsets.contains(34))
        assertFalse(gpio.allocation.offsets.contains(35))
    }

    private fun assertExpectedLines(index: ReadonlyHardwareAllocationIndex) {
        val expectedGpios =
            listOf(2, 3, 4, 5, 6, 12, 13, 16, 17, 19, 20, 21, 22, 23, 24, 25, 26, 27, 30, 31, 50, 51, 52)

        assertTrue(index.containsName("GPIO"))
        assertTrue(index.containsName("SPI0"))
        assertTrue(index.containsName("PWM0"))
        assertTrue(index.containsName("PWM1"))
        assertTrue(index.containsName("I2C3"))
        assertTrue(index.containsName("UART1"))
        expectedGpios.forEach { assertTrue(index.containsPin(it)) }

        val gpio = assertNotNull(index.findByName("GPIO"))
        val spi0 = assertNotNull(index.findByName("SPI0"))
        val pwm0 = assertNotNull(index.findByName("PWM0"))
        val pwm1 = assertNotNull(index.findByName("PWM1"))
        val i2c3 = assertNotNull(index.findByName("I2C3"))
        val uart1 = assertNotNull(index.findByName("UART1"))
        assertEquals(LineType.GPIO, gpio.lineType)
        assertEquals(LineType.SPI, spi0.lineType)
        assertEquals(LineType.PWM, pwm0.lineType)
        assertEquals(LineType.PWM, pwm1.lineType)
        assertEquals(LineType.I2C, i2c3.lineType)
        assertEquals(LineType.UART, uart1.lineType)

        assertEquals(expectedGpios, gpio.allocation.offsets)

        assertEquals(listOf(9, 10, 11), spi0.allocation.offsets)
        assertEquals(listOf(18), pwm0.allocation.offsets)
        assertEquals(listOf(45), pwm1.allocation.offsets)
        assertEquals(listOf(14, 15), i2c3.allocation.offsets)
        assertEquals(listOf(0, 1), uart1.allocation.offsets)
    }
}
