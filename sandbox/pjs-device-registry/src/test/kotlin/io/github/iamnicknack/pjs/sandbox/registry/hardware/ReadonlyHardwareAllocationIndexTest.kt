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
        assertTrue(index.flatMap { it.allocation.toList() }.all { it in 0..63 })
    }

    @Test
    fun gpioIncludesEntriesRegardlessOfCurrentValue() {
        val index = ReadonlyHardwareAllocationIndex(lineSupplier.lines())
        val gpio = assertNotNull(index.findByName("GPIO"))
        val offsets = gpio.allocation.toList()
        assertTrue(offsets.contains(7))
        assertTrue(offsets.contains(8))
        assertTrue(offsets.contains(28))
        assertTrue(offsets.contains(29))
        assertTrue(offsets.contains(32))
        assertTrue(offsets.contains(33))
        assertTrue(offsets.contains(34))
        assertTrue(offsets.contains(35))
    }

    private fun assertExpectedLines(index: ReadonlyHardwareAllocationIndex) {
        val expectedGpios =
            listOf(
                2, 3, 4, 5, 6, 7, 8, 12, 13, 16, 17, 19, 20, 21, 22, 23, 24, 25, 26, 27,
                28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 44, 46, 47, 48, 49, 50, 51, 52, 53
            )

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

        assertEquals(expectedGpios, gpio.allocation.toList())

        assertEquals(listOf(9, 10, 11), spi0.allocation.toList())
        assertEquals(listOf(18), pwm0.allocation.toList())
        assertEquals(listOf(45), pwm1.allocation.toList())
        assertEquals(listOf(14, 15), i2c3.allocation.toList())
        assertEquals(listOf(0, 1), uart1.allocation.toList())
    }
}
