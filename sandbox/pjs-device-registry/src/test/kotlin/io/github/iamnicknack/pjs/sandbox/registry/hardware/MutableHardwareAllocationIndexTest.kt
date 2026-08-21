package io.github.iamnicknack.pjs.sandbox.registry.hardware

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.github.iamnicknack.pjs.sandbox.registry.hardware.HardwareAllocationIndex.Line
import org.junit.jupiter.api.Test

class MutableHardwareAllocationIndexTest {

    @Test
    fun test() {
        val index = MutableHardwareAllocationIndex { it.lineType }

        val lineToAdd = Line(HardwareAllocationIndex.LineType.GPIO, "GPIO-0-1", HardwareAllocation.fromOffsets(0, 1))
        index.add(lineToAdd)

        assertThat(index.findByPin(0)).isEqualTo(lineToAdd)
    }
}
