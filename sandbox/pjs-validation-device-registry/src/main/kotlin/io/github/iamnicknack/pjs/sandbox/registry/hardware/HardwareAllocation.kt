package io.github.iamnicknack.pjs.sandbox.registry.hardware

import java.util.stream.IntStream

/**
 * Represents a hardware allocation of pin offsets and bitmasks.
 * @param mask the bitmask of offsets
 */
data class HardwareAllocation(
    val mask: Long
) : Iterable<Int> {

    /**
     * @param offsets the offsets / pins
     */
    constructor(offsets: List<Int>) : this(offsets.fold(0L) { acc, offset -> acc or (1L shl offset) })

    /**
     * Create a new [HardwareAllocation] from the intersection of this and another allocation.
     */
    infix fun and(other: HardwareAllocation): HardwareAllocation = HardwareAllocation(this.mask and other.mask)

    /**
     * Create a new [HardwareAllocation] from the union of this and another allocation.
     */
    infix fun or(other: HardwareAllocation): HardwareAllocation = HardwareAllocation(this.mask or other.mask)

    /**
     * Create a new [HardwareAllocation] from the difference of this and another allocation.
     *
     * Removes the pins from this allocation that are also in the other allocation.
     */
    infix fun not(other: HardwareAllocation): HardwareAllocation = HardwareAllocation(this.mask and other.mask.inv())

    /**
     * Check if this [HardwareAllocation] intersects with another allocation.
     */
    infix fun intersects(other: HardwareAllocation): Boolean = (this.mask and other.mask) != 0L

    /**
     * Check if this [HardwareAllocation] contains the entirety of another allocation.
     */
    infix fun contains(other: HardwareAllocation): Boolean = (this.mask and other.mask) == other.mask

    /**
     * Check if this [HardwareAllocation] contains a given pin.
     */
    fun contains(pin: Int): Boolean = mask and (1L shl pin) != 0L

    override fun iterator(): Iterator<Int> = OffsetsIterator(mask)

    companion object {
        /**
         * Creates a HardwareAllocation from a list of offsets / pins.
         * @param offsets the offsets / pins
         */
        @JvmStatic
        fun fromOffsets(vararg offsets: Int) = HardwareAllocation(offsets.toList())

        @JvmStatic
        @Suppress("SpreadOperator")
        fun fromBitCount(bitCount: Int) = fromOffsets(*IntStream.range(0, bitCount).toArray())
    }

    /**
     * An iterator over the set bits in a mask.
     */
    private class OffsetsIterator(private var remaining: Long) : Iterator<Int> {
        override fun hasNext(): Boolean {
            return remaining != 0L
        }

        override fun next(): Int {
            if (!hasNext()) throw NoSuchElementException()
            val index = java.lang.Long.numberOfTrailingZeros(remaining)
            remaining = remaining and (1L shl index).inv()
            return index
        }
    }
}
