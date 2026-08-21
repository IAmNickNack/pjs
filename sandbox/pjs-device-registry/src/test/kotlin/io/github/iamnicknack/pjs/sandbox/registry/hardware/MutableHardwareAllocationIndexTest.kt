package io.github.iamnicknack.pjs.sandbox.registry.hardware

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import io.github.iamnicknack.pjs.sandbox.registry.hardware.HardwareAllocationIndex.Line
import org.junit.jupiter.api.Test

class MutableHardwareAllocationIndexTest {

    @Test
    fun `can add line`() {
        val index = MutableHardwareAllocationIndex()

        val lineToAdd = Line(HardwareAllocationIndex.LineType.GPIO, "GPIO-0-1", HardwareAllocation.fromOffsets(0, 1))
        index.add(lineToAdd)

        assertThat(index.findByPin(0)).isEqualTo(lineToAdd)
        assertThat(index.findByPin(1)).isEqualTo(lineToAdd)
        assertThat(index.findByName(lineToAdd.name)).isEqualTo(lineToAdd)
    }

    @Test
    fun `can remove line`() {
        val line = Line(HardwareAllocationIndex.LineType.GPIO, "GPIO-0-1", HardwareAllocation.fromOffsets(0, 1))
        val index = MutableHardwareAllocationIndex(line)

        index.remove(line)

        assertThat(index.findByPin(0)).isNull()
        assertThat(index.findByPin(1)).isNull()
        assertThat(index.findByName(line.name)).isNull()
    }
}
