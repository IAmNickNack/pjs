package io.github.iamnicknack.pjs.sandbox.registry.hardware

/**
 * Mutable implementation of [HardwareAllocationIndex] which allows lines to be added at runtime
 */
class MutableHardwareAllocationIndex<K>(
    private val keySelector: (HardwareAllocationIndex.Line) -> K
) : HardwareAllocationIndex.Mutable, HardwareAllocationIndex.Keyed<K> {

    /**
     * [HardwareAllocation] indicating current allocation of all device types
     */
    private var hardwareAllocation: HardwareAllocation = HardwareAllocation.EMPTY

    /**
     * The index of allocations keyed by the result of [keySelector]
     */
    private val indexByKey: MutableMap<K, Node> = mutableMapOf()

    /**
     * Adds a new line to the index
     * @param line the line to add
     */
    override fun add(line: HardwareAllocationIndex.Line): MutableHardwareAllocationIndex<K> {
        val key = keySelector(line)
        val current = indexByKey[key] ?: Node.EMPTY

        (current.allocation and line.allocation).takeIf { it != HardwareAllocation.EMPTY }
            ?.also { throw IllegalArgumentException("Allocation for $key already exists: $it") }

        hardwareAllocation = hardwareAllocation or line.allocation
        indexByKey[key] = Node(current.allocation or line.allocation, current.lines + line)

        return this
    }

    /**
     * Remove a line from the index
     * @param line the line to remove
     */
    override fun remove(line: HardwareAllocationIndex.Line): MutableHardwareAllocationIndex<K> {
        val key = keySelector(line)
        val current = indexByKey[key] ?: Node.EMPTY

        if (!current.lines.contains(line)) {
            return this
        }

        hardwareAllocation = hardwareAllocation not line.allocation
        indexByKey[key] = Node(current.allocation not line.allocation, current.lines - line)

        return this
    }

    override operator fun get(key: K): Set<HardwareAllocationIndex.Line> = indexByKey[key]?.lines ?: emptySet()

    override fun iterator(): Iterator<HardwareAllocationIndex.Line> = indexByKey.values.flatMap { it.lines }.iterator()

    private data class Node(
        val allocation: HardwareAllocation,
        val lines: Set<HardwareAllocationIndex.Line>
    ) {
        companion object {
            @JvmStatic
            val EMPTY = Node(HardwareAllocation.EMPTY, emptySet())
        }
    }

    companion object {
        @JvmStatic
        fun byLineType(vararg lines: HardwareAllocationIndex.Line): MutableHardwareAllocationIndex<HardwareAllocationIndex.LineType> {
            return MutableHardwareAllocationIndex {
                it.lineType
            }.apply {
                lines.forEach { add(it) }
            }
        }
    }
}