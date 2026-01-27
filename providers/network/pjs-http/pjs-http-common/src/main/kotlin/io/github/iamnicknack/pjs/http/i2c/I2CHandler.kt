package io.github.iamnicknack.pjs.http.i2c

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import io.github.iamnicknack.pjs.device.i2c.I2C
import io.github.iamnicknack.pjs.device.i2c.I2CConfig
import io.github.iamnicknack.pjs.http.config.ConfigHandler
import io.github.iamnicknack.pjs.http.jackson.Base64ByteArrayDeserializer
import io.github.iamnicknack.pjs.http.jackson.Base64ByteArraySerializer

interface I2CHandler : ConfigHandler<I2C> {

    suspend fun transfer(deviceId: String, payload: I2CTransferPayload): I2CTransferPayload

    @JsonIgnoreProperties(ignoreUnknown = true)
    class I2CConfigPayload(
        val bus: Int?,
    ) : ConfigHandler.DeviceConfigPayload<I2C> {
        override fun asDeviceConfig(deviceId: String?): I2CConfig = I2CConfig.builder()
            .id(deviceId ?: "I2C[$bus]")
            .bus(bus ?: 0)
            .build()
    }

    class I2CTransferPayload(
        val messages: List<I2CTransferMessage>
    )

    class I2CTransferMessage(
        val address: Int,
        @param:JsonProperty("payload")
        @param:JsonSerialize(using = Base64ByteArraySerializer::class)
        @param:JsonDeserialize(using = Base64ByteArrayDeserializer::class)
        val payload: ByteArray,
        val type: I2C.Message.Type
    )
}