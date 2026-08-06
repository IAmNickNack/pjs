package io.github.iamnicknack.pjs.sandbox.registry.hardware

import java.io.StringReader

open class BasicHardwareAllocationIndex(
    lines: Set<HardwareAllocationIndex.Line> = mutableSetOf()
) : HardwareAllocationIndex {
    private val linesByName = lines.associateBy { it.name }
    private val linesByPin = lines
        .flatMap { line -> line.allocation.offsets.map { it to line } }
        .toMap()
    private val linesByType = lines.groupBy { it.lineType }

    override fun findByName(name: String) = linesByName[name]

    override fun findByPin(pin: Int) = linesByPin[pin]

    override fun findAllByType(lineType: HardwareAllocationIndex.LineType) = linesByType[lineType]?.toSet() ?: emptySet()

    override fun indexForType(lineType: HardwareAllocationIndex.LineType): HardwareAllocationIndex = IndexByType(lineType)

    override fun iterator(): Iterator<HardwareAllocationIndex.Line> = linesByName.values.iterator()

    companion object {
        @JvmStatic
        fun fromPinctrlOutput(output: String): BasicHardwareAllocationIndex =
            BasicHardwareAllocationIndex(PinctrlParser().parse(StringReader(output)))
    }

    private inner class IndexByType(lineType: HardwareAllocationIndex.LineType) : BasicHardwareAllocationIndex(
        this@BasicHardwareAllocationIndex.findAllByType(lineType)
    )
}