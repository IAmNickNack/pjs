package io.github.iamnicknack.pjs.http.gpio

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.github.iamnicknack.pjs.device.gpio.GpioPort
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig
import io.github.iamnicknack.pjs.device.gpio.GpioPortMode
import io.github.iamnicknack.pjs.http.config.ConfigHandler
import io.github.iamnicknack.pjs.model.event.GpioChangeEventType

/**
 * HTTP layer functionality for GPIO ports
 */
interface GpioPortHandler : ConfigHandler<GpioPort> {

    /**
     * Read the current value of the device
     * @param deviceId the device id
     */
    suspend fun readDevice(deviceId: String): Int

    /**
     * Write the value to the device
     * @param deviceId the device id
     * @param value the value to write
     */
    suspend fun writeDevice(deviceId: String, value: Int)

    /**
     * Configuration payload for a GPIO port
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    class GpioPortConfigPayload(
        val pinNumber: IntArray,
        val mode: GpioPortMode,
        val defaultValue: Int? = null
    ) : ConfigHandler.DeviceConfigPayload<GpioPort> {

        override fun asDeviceConfig(deviceId: String?): GpioPortConfig = GpioPortConfig.builder()
            .id(deviceId ?: "GPIO[${this.pinNumber.joinToString(",")}")
            .pin(*this.pinNumber)
            .mode(this.mode)
            .defaultValue(this.defaultValue ?: 0)
            .build()
    }

    /**
     * Extended event types for SSE
     */
    enum class SseEventType(
        val value: Int,
        val gpioEvent: GpioChangeEventType
    ) {
        NONE(0, GpioChangeEventType.NONE),
        RISING(1, GpioChangeEventType.RISING),
        FALLING(2, GpioChangeEventType.FALLING),
        ANY(3, GpioChangeEventType.ANY),
        CONNECTED(4, GpioChangeEventType.NONE),
        DISCONNECTED(5, GpioChangeEventType.NONE);

        companion object {
            fun from(value: Int): SseEventType = when(value) {
                0 -> NONE
                1 -> RISING
                2 -> FALLING
                3 -> ANY
                4 -> CONNECTED
                5 -> DISCONNECTED
                else -> throw IllegalArgumentException("Invalid event type: $value")
            }

            fun from(gpio: GpioChangeEventType): SseEventType = when(gpio) {
                GpioChangeEventType.NONE -> NONE
                GpioChangeEventType.RISING -> RISING
                GpioChangeEventType.FALLING -> FALLING
                GpioChangeEventType.ANY -> ANY
            }
        }
    }
}