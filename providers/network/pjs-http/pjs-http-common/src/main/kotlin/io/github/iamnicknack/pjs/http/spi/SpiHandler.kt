package io.github.iamnicknack.pjs.http.spi

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import io.github.iamnicknack.pjs.device.spi.Spi
import io.github.iamnicknack.pjs.device.spi.SpiConfig
import io.github.iamnicknack.pjs.http.config.ConfigHandler
import io.github.iamnicknack.pjs.http.jackson.Base64ByteArrayDeserializer
import io.github.iamnicknack.pjs.http.jackson.Base64ByteArraySerializer

/**
 * HTTP layer functionality for SPI devices
 */
interface SpiHandler : ConfigHandler<Spi> {

    /**
     * Perform an SPI transfer
     * @param deviceId the device id
     * @param payload the data to transfer
     */
    suspend fun transfer(deviceId: String, payload: TransferPayload): TransferPayload

    @JsonIgnoreProperties(ignoreUnknown = true)
    class SpiConfigPayload(
        val baudRate: Int?,
        val bus: Int,
        val chipSelect: Int,
        val mode: Int?,
        val bitsPerWord: Int?,
        val lsbFirst: Boolean?
    ) : ConfigHandler.DeviceConfigPayload<Spi> {
        override fun asDeviceConfig(deviceId: String?): SpiConfig = SpiConfig(
            baudRate ?: 1_000_000,
            bus,
            chipSelect,
            mode ?: 0,
            bitsPerWord ?: 8,
            lsbFirst ?: false,
            deviceId ?: "SPI[$bus.$chipSelect]"
        )
    }

    class TransferPayload(
        @param:JsonProperty("data")
        @param:JsonSerialize(using = Base64ByteArraySerializer::class)
        @param:JsonDeserialize(using = Base64ByteArrayDeserializer::class)
        val data: ByteArray
    )}