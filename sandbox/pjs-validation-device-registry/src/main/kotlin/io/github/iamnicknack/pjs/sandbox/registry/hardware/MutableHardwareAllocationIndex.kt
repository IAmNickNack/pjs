package io.github.iamnicknack.pjs.sandbox.registry.hardware

/**
 * Mutable implementation of [HardwareAllocationIndex] which allows lines to be added at runtime
 */
class MutableHardwareAllocationIndex(
    private val lines: MutableSet<HardwareAllocationIndex.Line> = mutableSetOf(),
) : HardwareAllocationIndex.Mutable {

    /**
     * Tracker for in-use hardware allocations
     */
    private var inUse: HardwareAllocation = lines
        .map { it.allocation }
        .fold(HardwareAllocations.EMPTY) { acc, allocation -> acc or allocation }

    /**
     * Public accessor to the in-use hardware allocations tracker
     */
    val inUseAllocation: HardwareAllocation
        get() = inUse

    constructor(vararg lines: HardwareAllocationIndex.Line) : this(lines.toSet().toMutableSet())

    override fun add(line: HardwareAllocationIndex.Line): HardwareAllocationIndex.Mutable {
        require(inUse and line.allocation == HardwareAllocations.EMPTY) {
            "Line intersects with existing lines"
        }
        lines.add(line)
        inUse = inUse or line.allocation
        return this
    }

    override fun remove(line: HardwareAllocationIndex.Line): HardwareAllocationIndex.Mutable {
        require(lines.contains(line)) {
            "Line does not exist"
        }
        lines.remove(line)
        inUse = inUse not line.allocation
        return this
    }

    override fun iterator(): Iterator<HardwareAllocationIndex.Line> = lines.iterator()
}
