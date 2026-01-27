package io.github.iamnicknack.pjs.http.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.util.*

/**
 * Jackson deserializer that decodes base64 string to ByteArray
 */
class Base64ByteArrayDeserializer : JsonDeserializer<ByteArray>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ByteArray {
        val base64String = p.text
        return Base64.getDecoder().decode(base64String)
    }
}
