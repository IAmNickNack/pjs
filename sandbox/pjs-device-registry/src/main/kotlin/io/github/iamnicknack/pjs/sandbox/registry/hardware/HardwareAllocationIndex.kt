package io.github.iamnicknack.pjs.sandbox.registry.hardware

interface HardwareAllocationIndex : Iterable<HardwareAllocationIndex.Line> {

    /**
     * Fetch the line allocation for a given name
     * @param name the name of the line
     * @return the line allocation for a given name
     */
    fun findByName(name: String): Line?

    /**
     * Check if the index contains a line with the specified name
     * @param name the name of the line
     * @return true if the index contains a line with the specified name, false otherwise
     */
    fun containsName(name: String): Boolean = findByName(name) != null

    /**
     * Fetch the line allocation for a given pin
     * @param pin the pin number
     * @return the line allocation for a given pin
     */
    fun findByPin(pin: Int): Line?

    /**
     * Check if the index contains a line with the specified pin
     * @param pin the pin number
     * @return true if the index contains a line with the specified pin, false otherwise
     */
    fun containsPin(pin: Int): Boolean = findByPin(pin) != null

    /**
     * Fetch the line allocation which can satisfy the specified mask
     * @param mask the mask to match
     * @return the line allocation for a given mask
     */
    fun findByMask(mask: Long): Line? = this.firstOrNull { it.allocation.mask and mask == mask }

    /**
     * Fetch all line allocations with a mask that intersects with the specified mask
     * @param mask the mask to match
     * @return all line allocations with a mask that intersects with the specified mask
     */
    fun findAllIntersectingByMask(mask: Long): Set<Line> = this
        .filter { it.allocation.mask and mask != 0L }
        .toSet()

    /**
     * Fetch all line allocations of the specified type
     * @param lineType the type of line to fetch
     * @return all line allocations of the specified type
     */
    fun findAllByType(lineType: LineType): Set<Line>

    /**
     * Fetch an index of line allocations of the specified type
     * @param lineType the type of line to fetch
     * @return an index for the specified line type
     */
    fun indexForType(lineType: LineType): HardwareAllocationIndex

    /**
     * Check if the index contains a line allocation of the specified type
     * @param lineType the type of line to check for
     * @return true if the index contains a line allocation of the specified type, false otherwise
     */
    fun containsType(lineType: LineType): Boolean = findAllByType(lineType).isNotEmpty()

    /**
     * Calculate the remainder of [allocation] after negating all valid allocations in the index
     * @param allocation the allocation to check
     * @return the remainder of [allocation] representing pins not available in this index
     */
    fun remainder(allocation: HardwareAllocation): HardwareAllocation = this
        .fold(allocation.mask) { acc, m -> acc and m.allocation.mask.inv() }
        .let { HardwareAllocation.fromMask(it) }


    /**
     * Represents a line allocation in the hardware allocation index.
     * @param lineType the IO type of line
     * @param name the name of the line
     * @param allocation the hardware allocation of the line
     * @param bus the device bus number the line is connected to
     */
    data class Line(
        val lineType: LineType,
        val name: String,
        val allocation: HardwareAllocation,
        val bus: Int? = null,
    )

    /**
     * Enum of valid line types
     */
    enum class LineType {
        GPIO,
        SPI,
        PWM,
        I2C,
        UART
    }
}