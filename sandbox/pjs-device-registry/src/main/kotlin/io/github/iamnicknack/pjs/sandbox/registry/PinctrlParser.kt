package io.github.iamnicknack.pjs.sandbox.registry

import io.github.iamnicknack.pjs.sandbox.registry.HardwareAllocationIndex.LineType
import java.io.BufferedReader
import java.io.InputStream
import java.io.Reader
import java.nio.charset.StandardCharsets

class PinctrlParser {
    fun parse(inputStream: InputStream): Set<HardwareAllocationIndex.Line> =
        inputStream.bufferedReader(StandardCharsets.UTF_8).use { parse(it) }

    fun parse(reader: Reader): Set<HardwareAllocationIndex.Line> {
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
                    allocation = HardwareAllocation(offsets.toList().sorted())
                )
            }
    }

    private fun parseLine(line: String): Pair<Int, LineKey>? {
        val match = linePattern.matchEntire(line) ?: return null
        val offset = match.groupValues[1].toIntOrNull() ?: return null
        if (offset !in 0..63) {
            return null
        }

        val currentValue = match.groupValues[2]
        val token = match.groupValues[3]
        val lineKey = classifyLineKey(offset = offset, token = token, currentValue = currentValue) ?: return null
        return offset to lineKey
    }

    private fun classifyLineKey(offset: Int, token: String, currentValue: String): LineKey? {
        val normalized = token.uppercase()

        if (normalized == "INPUT" || normalized == "OUTPUT" || normalized == "NONE") {
            return if (currentValue == "--") LineKey(LineType.GPIO, "GPIO$offset") else null
        }

        spiPattern.find(normalized)?.groupValues?.get(1)?.let {
            return LineKey(LineType.SPI, "SPI$it")
        }
        pwmPattern.find(normalized)?.groupValues?.get(1)?.let {
            return LineKey(LineType.PWM, "PWM$it")
        }
        i2cSdaSclPattern.find(normalized)?.groupValues?.get(1)?.let {
            return LineKey(LineType.I2C, "I2C$it")
        }
        i2cBscPattern.find(normalized)?.groupValues?.get(1)?.let {
            return LineKey(LineType.I2C, "I2C$it")
        }
        uartPattern.find(normalized)?.groupValues?.get(1)?.let {
            return LineKey(LineType.UART, "UART$it")
        }

        return null
    }

    private data class LineKey(
        val lineType: LineType,
        val name: String
    )

    companion object {
        private val linePattern = Regex("""^\s*(\d+):\s+.*?\|\s*(\S+)\s*//.*?=\s*(\S+)\s*$""")
        private val spiPattern = Regex("""SPI_?(\d+)""")
        private val pwmPattern = Regex("""PWM_?(\d+)""")
        private val i2cSdaSclPattern = Regex("""(?:SDA|SCL)_?(\d+)""")
        private val i2cBscPattern = Regex("""BSC(?:_M)?_?(\d+)""")
        private val uartPattern = Regex("""(?:UART(?:_[A-Z]+)?_|TXD|RXD|CTS|RTS)_?(\d+)""")
    }
}
