package io.github.iamnicknack.pjs.http.jackson

import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueSerializer
import java.util.*

/**
 * Jackson serializer that encodes ByteArray as base64 string
 */
class Base64ByteArraySerializer : ValueSerializer<ByteArray>() {
    override fun serialize(value: ByteArray?, gen: JsonGenerator, ctxt: SerializationContext) {
        if (value == null) {
            gen.writeNull()
        } else {
            gen.writeString(Base64.getEncoder().encodeToString(value))
        }
    }
}
