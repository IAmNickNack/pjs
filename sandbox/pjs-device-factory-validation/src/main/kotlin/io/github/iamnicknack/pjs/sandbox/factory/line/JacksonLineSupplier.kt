package io.github.iamnicknack.pjs.sandbox.factory.line

import com.fasterxml.jackson.annotation.JsonInclude
import io.github.iamnicknack.pjs.device.gpio.GpioPinMask
import io.github.iamnicknack.pjs.sandbox.factory.hardware.HardwareAllocation
import io.github.iamnicknack.pjs.sandbox.factory.hardware.HardwareAllocationIndex
import tools.jackson.core.JsonGenerator
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.ValueSerializer
import tools.jackson.databind.module.SimpleModule
import tools.jackson.dataformat.yaml.YAMLMapper
import tools.jackson.module.kotlin.kotlinModule
import tools.jackson.module.kotlin.readValue
import java.io.FileNotFoundException
import java.io.Writer
import java.nio.file.Files
import java.nio.file.Paths

class JacksonLineSupplier(
    val path: String
) : LineSupplier {

    override fun lines(): Set<HardwareAllocationIndex.Line> {
        val inputStream = if (Files.exists(Paths.get(path))) {
            Files.newInputStream(Paths.get(path))
        } else {
            javaClass.getResourceAsStream(path)
        }

        return inputStream
            ?.use { YAML_MAPPER.readValue<Set<HardwareAllocationIndex.Line>>(it).toSet() }
            ?: throw FileNotFoundException(path)
    }

    class HardwareAllocationSerializer : ValueSerializer<HardwareAllocation>() {
        override fun serialize(
            value: HardwareAllocation,
            gen: JsonGenerator,
            ctxt: SerializationContext
        ) {
            val offsets = GpioPinMask.offsets(value.mask)
            gen.writeArray(offsets, 0, offsets.size)
        }
    }

    @Suppress("SpreadOperator")
    class HardwareAllocationDeserializer : ValueDeserializer<HardwareAllocation>() {
        override fun deserialize(
            p: JsonParser,
            ctxt: DeserializationContext
        ): HardwareAllocation {
            val offsets = p.readValueAs(IntArray::class.java)
            return HardwareAllocation.fromOffsets(*offsets)
        }
    }

    companion object {
        private val module = SimpleModule()
            .addSerializer(HardwareAllocation::class.java, HardwareAllocationSerializer())
            .addDeserializer(HardwareAllocation::class.java, HardwareAllocationDeserializer())

        private val YAML_MAPPER: YAMLMapper = YAMLMapper.builder()
            .addModule(kotlinModule())
            .addModule(module)
            .changeDefaultPropertyInclusion { JsonInclude.Value.ALL_NON_NULL }
            .build()

        @JvmStatic
        fun dump(lineSupplier: LineSupplier, writer: Writer = System.out.writer()) {
            YAML_MAPPER.writeValue(writer, lineSupplier.lines())
        }
    }
}
