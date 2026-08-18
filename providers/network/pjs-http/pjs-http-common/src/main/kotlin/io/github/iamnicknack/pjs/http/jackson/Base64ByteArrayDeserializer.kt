package io.github.iamnicknack.pjs.http.jackson

import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer
import java.util.*

/**
 * Jackson deserializer that decodes base64 string to ByteArray
 */
class Base64ByteArrayDeserializer : ValueDeserializer<ByteArray>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ByteArray {
        val base64String = p.string
        return Base64.getDecoder().decode(base64String)
    }
}
