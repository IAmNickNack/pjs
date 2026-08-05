package io.github.iamnicknack.pjs.sandbox.registry

import io.github.iamnicknack.pjs.sandbox.registry.HardwareAllocationIndex.Line
import io.github.iamnicknack.pjs.sandbox.registry.HardwareAllocationIndex.LineType
import java.io.StringReader

open class HardwareAllocationIndexImpl(
    lines: Set<Line> = mutableSetOf()
) : HardwareAllocationIndex {
    private val linesByName = lines.associateBy { it.name }
    private val linesByPin = lines
        .flatMap { line -> line.allocation.offsets.map { it to line } }
        .toMap()
    private val linesByType = lines.groupBy { it.lineType }

    override fun findByName(name: String): Line? = linesByName[name]

    override fun findByPin(pin: Int): Line? = linesByPin[pin]

    override fun findAllByType(lineType: LineType): Set<Line> = linesByType[lineType]?.toSet() ?: emptySet()

    override fun indexForType(lineType: LineType): HardwareAllocationIndex = IndexByType(lineType)

    override fun iterator(): Iterator<Line> {
        return linesByName.values.iterator()
    }

    companion object {
        @JvmStatic
        fun fromPinctrlOutput(output: String): HardwareAllocationIndexImpl =
            HardwareAllocationIndexImpl(PinctrlParser().parse(StringReader(output)))
    }

    private inner class IndexByType(lineType: LineType) : HardwareAllocationIndexImpl(
        this@HardwareAllocationIndexImpl.findAllByType(lineType)
    )
}