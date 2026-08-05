package io.github.iamnicknack.pjs.sandbox.registry

import io.github.iamnicknack.pjs.sandbox.registry.HardwareAllocationIndex.LineType
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.StringReader
import java.nio.charset.StandardCharsets
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HardwareAllocationIndexImplTest {

    @Test
    fun showAllLines() {
        val index = HardwareAllocationIndexImpl.fromPinctrlOutput(readPinctrlOutput())
        for (line in index) {
            println(line)
        }
    }

    @Test
    fun parsesPinctrlOutputIntoExpectedGroupedLines() {
        val index = HardwareAllocationIndexImpl.fromPinctrlOutput(readPinctrlOutput())

        assertExpectedLines(index)
    }

    @Test
    fun pinctrlParserParsesFromReader() {
        val lines = PinctrlParser().parse(StringReader(readPinctrlOutput()))
        val index = HardwareAllocationIndexImpl(lines)

        assertExpectedLines(index)
    }

    @Test
    fun pinctrlParserParsesFromInputStream() {
        val input = ByteArrayInputStream(readPinctrlOutput().toByteArray(StandardCharsets.UTF_8))
        val lines = PinctrlParser().parse(input)
        val index = HardwareAllocationIndexImpl(lines)

        assertExpectedLines(index)
    }

    @Test
    fun excludesOffsetsAbove63() {
        val index = HardwareAllocationIndexImpl.fromPinctrlOutput(readPinctrlOutput())

        assertTrue(index.flatMap { it.allocation.offsets }.all { it in 0..63 })
    }

    @Test
    fun gpioOnlyIncludesEntriesWithDashedCurrentValue() {
        val index = HardwareAllocationIndexImpl.fromPinctrlOutput(readPinctrlOutput())
        assertFalse(index.containsName("GPIO7"))
        assertFalse(index.containsName("GPIO8"))
        assertFalse(index.containsName("GPIO28"))
        assertFalse(index.containsName("GPIO29"))
        assertFalse(index.containsName("GPIO32"))
        assertFalse(index.containsName("GPIO33"))
        assertFalse(index.containsName("GPIO34"))
        assertFalse(index.containsName("GPIO35"))
    }

    private fun assertExpectedLines(index: HardwareAllocationIndexImpl) {
        val expectedGpios =
            listOf(2, 3, 4, 5, 6, 12, 13, 16, 17, 19, 20, 21, 22, 23, 24, 25, 26, 27, 30, 31, 50, 51, 52)

        assertFalse(index.containsName("GPIO"))
        assertTrue(index.containsName("SPI0"))
        assertTrue(index.containsName("PWM0"))
        assertTrue(index.containsName("PWM1"))
        assertTrue(index.containsName("I2C3"))
        assertTrue(index.containsName("UART1"))
        expectedGpios.forEach { assertTrue(index.containsName("GPIO$it")) }

        val spi0 = assertNotNull(index.findByName("SPI0"))
        val pwm0 = assertNotNull(index.findByName("PWM0"))
        val pwm1 = assertNotNull(index.findByName("PWM1"))
        val i2c3 = assertNotNull(index.findByName("I2C3"))
        val uart1 = assertNotNull(index.findByName("UART1"))
        assertEquals(LineType.SPI, spi0.lineType)
        assertEquals(LineType.PWM, pwm0.lineType)
        assertEquals(LineType.PWM, pwm1.lineType)
        assertEquals(LineType.I2C, i2c3.lineType)
        assertEquals(LineType.UART, uart1.lineType)

        expectedGpios.forEach {
            val gpioLine = assertNotNull(index.findByName("GPIO$it"))
            assertEquals(LineType.GPIO, gpioLine.lineType)
            assertEquals(listOf(it), gpioLine.allocation.offsets)
        }

        assertEquals(listOf(9, 10, 11), spi0.allocation.offsets)
        assertEquals(listOf(18), pwm0.allocation.offsets)
        assertEquals(listOf(45), pwm1.allocation.offsets)
        assertEquals(listOf(14, 15), i2c3.allocation.offsets)
        assertEquals(listOf(0, 1), uart1.allocation.offsets)
        assertNull(index.findByName("GPIO"))
    }

    private fun readPinctrlOutput(): String {
        val input = javaClass.getResourceAsStream("/pinctrl-output.txt")
            ?: throw IOException("Missing test resource pinctrl-output.txt")

        return input.use { String(it.readAllBytes(), StandardCharsets.UTF_8) }
    }
}
