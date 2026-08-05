package io.github.iamnicknack.pjs.sandbox.registry

interface KeyedHardwareAllocationIndex<KEY> : HardwareAllocationIndex {

    operator fun get(key: KEY): Set<HardwareAllocationIndex.Line>

    fun containsKey(key: KEY): Boolean = this[key].isNotEmpty()
}