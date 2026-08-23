package io.github.iamnicknack.pjs.sandbox.registry.hardware

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import io.github.iamnicknack.pjs.sandbox.registry.hardware.HardwareAllocationIndex.Line
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MutableHardwareAllocationIndexTest {

    @Test
    fun `can add line`() {
        val index = MutableHardwareAllocationIndex()

        val line = Line(HardwareAllocationIndex.LineType.GPIO, "GPIO-0-1", HardwareAllocation.fromOffsets(0, 1))
        index.add(line)

        assertThat(index.findByAllocation(line.allocation)).isNotNull()
        assertThat(index.findByName(line.name)).isEqualTo(line)
        assertThat(index.inUseAllocation).isNotEqualTo(HardwareAllocations.EMPTY)
    }

    @Test
    fun `can remove line`() {
        val line = Line(HardwareAllocationIndex.LineType.GPIO, "GPIO-0-1", HardwareAllocation.fromOffsets(0, 1))
        val index = MutableHardwareAllocationIndex(line)

        index.remove(line)

        assertThat(index.findByAllocation(line.allocation)).isNull()
        assertThat(index.findByName(line.name)).isNull()
        assertThat(index.inUseAllocation).isEqualTo(HardwareAllocations.EMPTY)
    }

    @Test
    fun `cannot re-add line`() {
        val line = Line(HardwareAllocationIndex.LineType.GPIO, "GPIO-0-1", HardwareAllocation.fromOffsets(0, 1))
        val index = MutableHardwareAllocationIndex(line)

        assertThrows<IllegalArgumentException> { index.add(line) }
    }

    @Test
    fun `can re-add line after removal`() {
        val line = Line(HardwareAllocationIndex.LineType.GPIO, "GPIO-0-1", HardwareAllocation.fromOffsets(0, 1))
        val index = MutableHardwareAllocationIndex(line)

        index.remove(line)
        assertThat(index.inUseAllocation).isEqualTo(HardwareAllocations.EMPTY)

        index.add(line)
        assertThat(index.findByAllocation(line.allocation)).isNotNull()
        assertThat(index.findByName(line.name)).isEqualTo(line)
        assertThat(index.inUseAllocation).isNotEqualTo(HardwareAllocations.EMPTY)
    }

}
