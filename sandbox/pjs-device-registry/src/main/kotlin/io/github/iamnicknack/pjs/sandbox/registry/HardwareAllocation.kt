package io.github.iamnicknack.pjs.sandbox.registry

data class HardwareAllocation(
    val offsets: List<Int>,
    val mask: Long = offsets.fold(0L) { acc, offset -> acc or (1L shl offset) }
) {
    /**
     * Create a new [HardwareAllocation] from the intersection of this and another allocation.
     */
    infix fun and(other: HardwareAllocation): HardwareAllocation = fromMask(this.mask and other.mask)

    /**
     * Create a new [HardwareAllocation] from the union of this and another allocation.
     */
    infix fun or(other: HardwareAllocation): HardwareAllocation = fromMask(this.mask or other.mask)

    /**
     * Check if this [HardwareAllocation] contains a given pin.
     */
    fun contains(pin: Int): Boolean = mask and (1L shl pin) != 0L

    /**
     * Check if this [HardwareAllocation] intersects with another allocation.
     */
    infix fun intersects(other: HardwareAllocation): Boolean = (this.mask and other.mask) != 0L

    companion object {
        @JvmStatic
        val EMPTY = fromMask(0L)

        /**
         * Creates a HardwareAllocation from a single offset / pin.
         * @param offset the offset / pin
         */
        @JvmStatic
        fun fromOffset(offset: Int) = HardwareAllocation(listOf(offset))

        /**
         * Creates a HardwareAllocation from a list of offsets / pins.
         * @param offsets the offsets / pins
         */
        @JvmStatic
        fun fromOffsets(vararg offsets: Int) = HardwareAllocation(offsets.toList())

        /**
         * Creates a HardwareAllocation from a mask, calculating the offsets.
         * @param mask the bitmask of offsets
         */
        @JvmStatic
        fun fromMask(mask: Long): HardwareAllocation {
            val offsets = (0..63)
                .mapNotNull { if (mask and (1L shl it) != 0L) it else null }

            return HardwareAllocation(offsets, mask)
        }
    }
}