package io.github.iamnicknack.pjs.sandbox.registry

class MutableHardwareAllocationIndex<T>(
    private val keySelector: (HardwareAllocationIndex.Line) -> T
) : HardwareAllocationIndex, KeyedHardwareAllocationIndex<T> {

    private var hardwareAllocation: HardwareAllocation = HardwareAllocation.EMPTY

    private val indexByKey: MutableMap<T, Node> = mutableMapOf()

    fun add(line: HardwareAllocationIndex.Line): MutableHardwareAllocationIndex<T> {

        val key = keySelector(line)
        val current = indexByKey[key] ?: Node.EMPTY

        (current.allocation and line.allocation).takeIf { it != HardwareAllocation.EMPTY }
            ?.also { throw IllegalArgumentException("Allocation for $key already exists: $it") }

        hardwareAllocation = hardwareAllocation or line.allocation
        indexByKey[key] = Node(current.allocation or line.allocation, current.lines + line)

        return this
    }

    override operator fun get(key: T): Set<HardwareAllocationIndex.Line> = indexByKey[key]?.lines ?: emptySet()

    override fun findByName(name: String): HardwareAllocationIndex.Line? = indexByKey.values
        .flatMap { it.lines }
        .firstOrNull { it.name == name }

    override fun findByPin(pin: Int): HardwareAllocationIndex.Line? = indexByKey.values
        .firstOrNull { it.allocation.contains(pin) }
        ?.lines
        ?.firstOrNull { it.allocation.contains(pin) }

    override fun findAllByType(lineType: HardwareAllocationIndex.LineType): Set<HardwareAllocationIndex.Line> = indexByKey.values
        .flatMap { it.lines }
        .filter { it.lineType == lineType }
        .toSet()

    override fun indexForType(lineType: HardwareAllocationIndex.LineType): HardwareAllocationIndex {
        return HardwareAllocationIndexImpl(findAllByType(lineType))
    }

    fun <T> indexForType(
        lineType: HardwareAllocationIndex.LineType,
        keySelector: (HardwareAllocationIndex.Line) -> T
    ): KeyedHardwareAllocationIndex<T> {
        return MutableHardwareAllocationIndex(keySelector).apply {
            this.findAllByType(lineType).forEach { add(it) }
        }
    }

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
}