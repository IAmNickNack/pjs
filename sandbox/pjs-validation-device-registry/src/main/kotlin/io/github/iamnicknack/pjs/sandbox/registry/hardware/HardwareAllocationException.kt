package io.github.iamnicknack.pjs.sandbox.registry.hardware

/**
 * An exception thrown when attempting to allocate hardware.
 */
sealed class HardwareAllocationException(message: String) : RuntimeException(message) {
    /**
     * Exception thrown when attempting to allocate pins that are not available.
     * @param requested the requested line
     * @param unavailable the unavailable lines
     */
    class PinsNotAvailable(
        val requested: HardwareAllocationIndex.Line,
        val unavailable: HardwareAllocation
    ) : HardwareAllocationException(
        "Pins not available: ${requested.name}, unavailable: ${unavailable.joinToString(", ")}"
    )
    /**
     * Exception thrown when attempting to allocate hardware that is already in use.
     *
     * @param requested the requested line
     * @param conflicts the lines already in use which conflict with [requested]
     */
    class PinsInUse(
        val requested: HardwareAllocationIndex.Line,
        conflicts: Iterable<HardwareAllocationIndex.Line>
    ) : HardwareAllocationException(
        "Pins in use: ${requested.name}, conflicts: ${conflicts.joinToString(", ") { it.name }}")
    {
        val conflicts: List<HardwareAllocationIndex.Line> = conflicts
            .map { HardwareAllocationIndex.Line(it.lineType, it.name, it.allocation and requested.allocation) }
    }

    /**
     * Exception thrown when attempting to allocate hardware on a bus that is already in use.
     * @param requested the requested bus
     * @param current the line already using the bus
     */
    class BusInUse(
        val requested: HardwareAllocationIndex.Line,
        val current: HardwareAllocationIndex.Line
    ) : HardwareAllocationException(
        "Bus ${requested.bus} for ${requested.lineType} is already in use by ${current.name}"
    )

    /**
     * Exception thrown when attempting to allocate hardware on a bus that is not configured.
     */
    class BusNotConfigured(val bus: Int, val lineType: HardwareAllocationIndex.LineType) :
        HardwareAllocationException("Bus $bus is not configured for $lineType")

    /**
     * Exception thrown when attempting to allocate hardware on a channel that is not configured.
     * @param bus the bus the channel is on
     * @param channel the channel that is not configured
     * @param lineType the type of line the channel is for
     */
    class ChannelNotConfigured(
        val bus: Int,
        val channel: Int,
        val lineType: HardwareAllocationIndex.LineType
    ) : HardwareAllocationException("Channel $channel on bus $bus is not configured for $lineType")
}
