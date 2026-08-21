package io.github.iamnicknack.pjs.sandbox.registry.hardware

/**
 * Common [HardwareAllocation]s
 */
object HardwareAllocations {
    @JvmStatic
    val EMPTY = HardwareAllocation(0)

    @JvmStatic
    val RASPBERRY_PI = HardwareAllocation.fromBitCount(27)
}