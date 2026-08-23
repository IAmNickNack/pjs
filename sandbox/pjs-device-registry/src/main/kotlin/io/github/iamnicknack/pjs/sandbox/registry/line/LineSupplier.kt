package io.github.iamnicknack.pjs.sandbox.registry.line

import io.github.iamnicknack.pjs.sandbox.registry.hardware.HardwareAllocation
import io.github.iamnicknack.pjs.sandbox.registry.hardware.HardwareAllocationIndex
import io.github.iamnicknack.pjs.sandbox.registry.hardware.HardwareAllocations

/**
 * A component that provides a set of
 * [io.github.iamnicknack.pjs.sandbox.registry.hardware.HardwareAllocationIndex.Line] instances.
 */
fun interface LineSupplier {
    /**
     * Provides a set of [io.github.iamnicknack.pjs.sandbox.registry.hardware.HardwareAllocationIndex.Line] instances.
     * @return a set of [io.github.iamnicknack.pjs.sandbox.registry.hardware.HardwareAllocationIndex.Line] instances
     */
    fun lines(): Set<HardwareAllocationIndex.Line>

    /**
     * Utility to cache the result of [lines] to avoid repeated computation.
     */
    fun cached(): LineSupplier {
        val lines = this.lines()
        return {
            lines
        }
    }

    /**
     * Filters the lines to only include those that are allocated in the given [hardwareAllocation].
     * @param hardwareAllocation the hardware allocation to filter by
     * @return a new [LineSupplier] that only includes lines allocated in the given [hardwareAllocation]
     */
    fun forHardwareAllocation(hardwareAllocation: HardwareAllocation) = LineSupplier {
        this@LineSupplier.lines()
            .map { it.copy(allocation = it.allocation and hardwareAllocation) }
            .filter { it.allocation != HardwareAllocations.EMPTY }
            .toSet()
    }
}
