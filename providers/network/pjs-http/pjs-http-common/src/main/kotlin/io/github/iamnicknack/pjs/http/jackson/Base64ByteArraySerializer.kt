package io.github.iamnicknack.pjs.http.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.util.*

/**
 * Jackson serializer that encodes ByteArray as base64 string
 */
class Base64ByteArraySerializer : JsonSerializer<ByteArray>() {
    override fun serialize(value: ByteArray?, gen: JsonGenerator, serializers: SerializerProvider) {
        if (value == null) {
            gen.writeNull()
        } else {
            gen.writeString(Base64.getEncoder().encodeToString(value))
        }
    }
}
