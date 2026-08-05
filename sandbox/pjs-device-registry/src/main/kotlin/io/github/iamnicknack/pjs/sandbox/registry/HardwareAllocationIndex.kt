package io.github.iamnicknack.pjs.sandbox.registry

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
     * Fetch all line allocations with a mask that intersects with the specified mask
     * @param mask the mask to match
     * @return all line allocations with a mask that intersects with the specified mask
     */
    fun findAllByMask(mask: Long): Set<Line> = this
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
     * Represents a line allocation in the hardware allocation index.
     */
    data class Line(
        val lineType: LineType,
        val name: String,
        val allocation: HardwareAllocation
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