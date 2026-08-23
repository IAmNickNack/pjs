package io.github.iamnicknack.pjs.sandbox.registry.line

import io.github.iamnicknack.pjs.sandbox.registry.hardware.HardwareAllocation
import io.github.iamnicknack.pjs.sandbox.registry.hardware.HardwareAllocationIndex
import java.io.BufferedReader
import java.io.InputStream
import java.io.Reader
import java.nio.charset.StandardCharsets

internal object PinctrlParser {
    fun readLines(inputStream: InputStream): Set<HardwareAllocationIndex.Line> =
        inputStream.bufferedReader(StandardCharsets.UTF_8).use { readLines(it) }

    fun readLines(reader: Reader): Set<HardwareAllocationIndex.Line> {
        val allocations = mutableMapOf<LineKey, MutableSet<Int>>()
        val bufferedReader = reader as? BufferedReader ?: reader.buffered()

        bufferedReader.lineSequence()
            .mapNotNull { parseLine(it) }
            .forEach { (offset, lineKey) ->
                allocations.getOrPut(lineKey) { mutableSetOf() }.add(offset)
            }

        return allocations
            .toList()
            .sortedBy { (key, _) -> key.name }
            .mapTo(linkedSetOf()) { (key, offsets) ->
                HardwareAllocationIndex.Line(
                    lineType = key.lineType,
                    name = key.name,
                    allocation = HardwareAllocation(offsets.toList().sorted()),
                    bus = key.bus
                )
            }
    }

    @Suppress("MagicNumber", "ReturnCount")
    private fun parseLine(line: String): Pair<Int, LineKey>? {
        val match = linePattern.matchEntire(line) ?: return null
        val offset = match.groupValues[1].toIntOrNull() ?: return null
        if (offset !in 0..63) {
            return null
        }

        val token = match.groupValues[3]
        val lineKey = classifyLineKey(token = token) ?: return null
        return offset to lineKey
    }

    @Suppress("ReturnCount")
    private fun classifyLineKey(token: String): LineKey? {
        val normalized = token.uppercase()

        if (normalized == "INPUT" || normalized == "OUTPUT" || normalized == "NONE") {
            return LineKey(HardwareAllocationIndex.LineType.GPIO, "GPIO")
        }

        spiPattern.find(normalized)?.groupValues?.get(1)?.let {
            return LineKey(HardwareAllocationIndex.LineType.SPI, "SPI$it", it.toIntOrNull())
        }
        pwmPattern.find(normalized)?.groupValues?.get(1)?.let {
            return LineKey(HardwareAllocationIndex.LineType.PWM, "PWM$it", it.toIntOrNull())
        }
        i2cSdaSclPattern.find(normalized)?.groupValues?.get(1)?.let {
            return LineKey(HardwareAllocationIndex.LineType.I2C, "I2C$it", it.toIntOrNull())
        }
        i2cBscPattern.find(normalized)?.groupValues?.get(1)?.let {
            return LineKey(HardwareAllocationIndex.LineType.I2C, "I2C$it", it.toIntOrNull())
        }
        uartPattern.find(normalized)?.groupValues?.get(1)?.let {
            return LineKey(HardwareAllocationIndex.LineType.UART, "UART$it", it.toIntOrNull())
        }

        return null
    }

    private data class LineKey(
        val lineType: HardwareAllocationIndex.LineType,
        val name: String,
        val bus: Int? = null
    )

    private val linePattern = Regex("""^\s*(\d+):\s+.*?\|\s*(\S+)\s*//.*?=\s*(\S+)\s*$""")
    private val spiPattern = Regex("""SPI_?(\d+)""")
    private val pwmPattern = Regex("""PWM_?(\d+)""")
    private val i2cSdaSclPattern = Regex("""(?:SDA|SCL)_?(\d+)""")
    private val i2cBscPattern = Regex("""BSC(?:_M)?_?(\d+)""")
    private val uartPattern = Regex("""(?:UART(?:_[A-Z]+)?_|TXD|RXD|CTS|RTS)_?(\d+)""")
}
