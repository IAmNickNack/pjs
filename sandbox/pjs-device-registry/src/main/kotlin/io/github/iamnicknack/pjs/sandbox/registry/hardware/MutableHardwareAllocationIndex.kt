package io.github.iamnicknack.pjs.sandbox.registry.hardware

/**
 * Mutable implementation of [HardwareAllocationIndex] which allows lines to be added at runtime
 */
class MutableHardwareAllocationIndex(
    private val lines: MutableSet<HardwareAllocationIndex.Line> = mutableSetOf()
) : HardwareAllocationIndex.Mutable {

    constructor(vararg lines: HardwareAllocationIndex.Line) : this(lines.toSet().toMutableSet())

    override fun add(line: HardwareAllocationIndex.Line): HardwareAllocationIndex.Mutable {
        lines.add(line)
        return this
    }

    override fun remove(line: HardwareAllocationIndex.Line): HardwareAllocationIndex.Mutable {
        lines.remove(line)
        return this
    }

    override fun iterator(): Iterator<HardwareAllocationIndex.Line> = lines.iterator()
}
