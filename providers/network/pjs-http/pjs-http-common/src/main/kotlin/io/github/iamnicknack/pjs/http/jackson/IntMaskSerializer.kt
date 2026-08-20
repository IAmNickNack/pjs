package io.github.iamnicknack.pjs.http.jackson

import io.github.iamnicknack.pjs.device.gpio.GpioPinMask
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueSerializer

/**
 * Helper to convert an array of pin offsets to a masked integer
 *
 * This allows the API to expose the internal bitmask as an array of pin offsets
 */
class IntMaskSerializer : ValueSerializer<IntArray>() {
    override fun serialize(
        value: IntArray,
        gen: JsonGenerator,
        ctxt: SerializationContext
    ) {
        gen.writeNumber(GpioPinMask.mask(*value))
    }
}