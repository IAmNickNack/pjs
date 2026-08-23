package io.github.iamnicknack.pjs.http.jackson

import io.github.iamnicknack.pjs.device.gpio.GpioPinMask
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer

/**
 * Helper to convert a masked integer to an array of pin offsets
 *
 * This allows the API to expose the internal bitmask as an array of pin offsets
 */
class IntMaskDeserializer : ValueDeserializer<IntArray>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext
    ): IntArray {
        return GpioPinMask.offsets(p.longValue)
    }
}