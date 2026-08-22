package io.github.iamnicknack.pjs.sandbox.registry.hardware

import io.github.iamnicknack.pjs.sandbox.registry.hardware.HardwareAllocationIndex.Line
import io.github.iamnicknack.pjs.sandbox.registry.hardware.HardwareAllocationIndex.LineType


/**
 * A readonly implementation of [HardwareAllocationIndex] that queries a predefined set of [Line]s.
 * @param lines the set of lines to read from
 */
open class ReadonlyHardwareAllocationIndex(lines: Set<Line>) : HardwareAllocationIndex {

    constructor(vararg lines: Line) : this(lines.toSet())

    private val linesByName = lines
        .associateBy { it.name }
    private val linesByType = lines
        .groupBy { it.lineType }

    override fun findByName(name: String) = linesByName[name]

    override fun findAllByType(lineType: LineType) = linesByType[lineType]?.toSet() ?: emptySet()

    override fun indexForType(lineType: LineType): HardwareAllocationIndex = IndexByType(lineType)

    override fun iterator(): Iterator<Line> = linesByName.values.iterator()

    private inner class IndexByType(lineType: LineType) : ReadonlyHardwareAllocationIndex(
        this@ReadonlyHardwareAllocationIndex.findAllByType(lineType)
    )
}
