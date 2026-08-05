package io.github.iamnicknack.pjs.sandbox.registry

import io.github.iamnicknack.pjs.sandbox.registry.HardwareAllocationIndex.Line
import org.junit.jupiter.api.Test

class MutableHardwareAllocationIndexTest {

    @Test
    fun test() {
        val index = MutableHardwareAllocationIndex { it.lineType }

        val lineToAdd = Line(HardwareAllocationIndex.LineType.GPIO, "GPIO-0-1", HardwareAllocation.fromOffsets(0, 1))
        index.add(lineToAdd)

        val line = index.findByPin(0)
        println(line)

    }

}