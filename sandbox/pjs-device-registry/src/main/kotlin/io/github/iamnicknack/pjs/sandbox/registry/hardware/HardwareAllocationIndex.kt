package io.github.iamnicknack.pjs.sandbox.registry.hardware

interface HardwareAllocationIndex : Iterable<HardwareAllocationIndex.Line> {

    /**
     * Fetch the line allocation for a given name
     * @param name the name of the line
     * @return the line allocation for a given name
     */
    fun findByName(name: String): Line? = this
        .firstOrNull { it.name == name }

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
    fun findByPin(pin: Int): Line? = this
        .firstOrNull { it.allocation.contains(pin) }

    /**
     * Check if the index contains a line with the specified pin
     * @param pin the pin number
     * @return true if the index contains a line with the specified pin, false otherwise
     */
    fun containsPin(pin: Int): Boolean = findByPin(pin) != null

    /**
     * Fetch the line allocation which can satisfy the specified allocation
     * @param allocation the allocation to match
     * @return the line allocation for a given allocation
     */
    fun findByAllocation(allocation: HardwareAllocation): Line? = this
        .firstOrNull { it.allocation contains allocation }

    /**
     * Fetch all line allocations which intersect the specified allocation
     * @param allocation the allocation to match
     * @return all line allocations which intersect with the specified allocation
     */
    fun findAllIntersectingByAllocation(allocation: HardwareAllocation): Set<Line> = this
        .filter { it.allocation intersects allocation }
        .toSet()

    /**
     * Fetch the line allocation which can satisfy the specified offsets
     * @param offsets the offsets to match
     * @return the line allocation for given offsets
     */
    fun findByOffsets(vararg offsets: Int): Line? = this
        .findByAllocation(HardwareAllocation.fromOffsets(*offsets))

    /**
     * Fetch the line allocation which can satisfy the specified mask
     * @param mask the mask to match
     * @return the line allocation for given mask
     */
    fun findByMask(mask: Long): Line? = this
        .findByAllocation(HardwareAllocation(mask))

    /**
     * Fetch all line allocations of the specified type
     * @param lineType the type of line to fetch
     * @return all line allocations of the specified type
     */
    fun findAllByType(lineType: LineType): Set<Line> = this
        .filter { it.lineType == lineType }
        .toSet()

    /**
     * Fetch an index of line allocations of the specified type
     * @param lineType the type of line to fetch
     * @return an index for the specified line type
     */
    fun indexForType(lineType: LineType): HardwareAllocationIndex = this
        .findAllByType(lineType)
        .let(::ReadonlyHardwareAllocationIndex)

    /**
     * Calculate the remainder of [allocation] after negating all valid allocations in the index
     * @param allocation the allocation to check
     * @return the remainder of [allocation] representing pins not available in this index
     */
    fun remainder(allocation: HardwareAllocation): HardwareAllocation = this
        .fold(HardwareAllocation(allocation.mask)) { acc, m -> acc not m.allocation }


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

    /**
     * Mutable hardware allocation which allows for adding and removing line allocations at runtime
     */
    interface Mutable : HardwareAllocationIndex {
        /**
         * Add a line allocation to the index
         * @param line the line to add
         * @return the updated mutable hardware allocation index
         */
        fun add(line: Line): Mutable
        /**
         * Remove a line allocation from the index
         * @param line the line to remove
         * @return the updated mutable hardware allocation index
         */
        fun remove(line: Line): Mutable
    }

    /**
     * Hardware allocation index which allows for retrieving line allocations a specific key type
     * @param K the key type
     */
    interface Keyed<K> : HardwareAllocationIndex {
        operator fun get(key: K): Set<Line>
    }
}