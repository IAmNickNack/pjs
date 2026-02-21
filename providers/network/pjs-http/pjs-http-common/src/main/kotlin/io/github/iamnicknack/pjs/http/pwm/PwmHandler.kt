package io.github.iamnicknack.pjs.http.pwm

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.github.iamnicknack.pjs.device.pwm.Pwm
import io.github.iamnicknack.pjs.device.pwm.PwmConfig
import io.github.iamnicknack.pjs.http.config.ConfigHandler
import io.github.iamnicknack.pjs.model.device.DeviceConfig

/**
 * HTTP layer functionality for PWM devices
 */
interface PwmHandler : ConfigHandler<Pwm> {

    suspend fun on(deviceId: String)
    suspend fun off(deviceId: String)

    suspend fun setDutyCycle(deviceId: String, dutyCycle: Long)
    suspend fun getDutyCycle(deviceId: String): Long

    suspend fun setPolarity(deviceId: String, polarity: Pwm.Polarity)
    suspend fun getPolarity(deviceId: String): Pwm.Polarity
//
//    suspend fun setFrequency(deviceId: String, frequency: Int)
//    suspend fun getFrequency(deviceId: String): Int

    suspend fun setPeriod(deviceId: String, period: Long)
    suspend fun getPeriod(deviceId: String): Long

    /**
     * Configuration payload for a PWM device
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    class PwmConfigPayload(
        val chip: Int?,
        val channel: Int?,
        val period: Long?,
        val polarity: Pwm.Polarity?,
        val dutyCycle: Long?
    ) : ConfigHandler.DeviceConfigPayload<Pwm> {
        override fun asDeviceConfig(deviceId: String?): DeviceConfig<Pwm> = PwmConfig.builder()
            .id(deviceId ?: "PWM[${chip ?: 0}.${channel ?: 0}]")
            .chip(chip ?: 0)
            .channel(channel ?: 0)
            .period(period ?: 1000) // TODO: set this to 440hz
            .polarity(polarity ?: Pwm.Polarity.NORMAL)
            .dutyCycle(dutyCycle ?: 50)
            .build()
    }
}