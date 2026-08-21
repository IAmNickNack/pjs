package io.github.iamnicknack.pjs.sandbox.registry.hardware

import java.io.IOException
import java.io.InputStream

/**
 * A component that provides a set of [HardwareAllocationIndex.Line] instances.
 */
fun interface LineSupplier {
    /**
     * Provides a set of [HardwareAllocationIndex.Line] instances.
     * @return a set of [HardwareAllocationIndex.Line] instances
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
            .filter { it != HardwareAllocations.EMPTY }
            .toSet()
    }

    companion object {
        /**
         * Reads output from `pinctrl`.
         */
        @JvmStatic
        fun fromPinctrl(stream: InputStream) = LineSupplier {
            PinctrlParser().readLines(stream)
        }.cached()

        /**
         * Reads `pinctrl` output from a classpath resource
         */
        @JvmStatic
        fun fromPinctrlResource(resourceName: String) = LineSupplier {
            val input = javaClass.getResourceAsStream(resourceName)
                ?: throw IOException("Missing resource $resourceName")
            PinctrlParser().readLines(input)
        }.cached()
    }
}
