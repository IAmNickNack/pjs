package io.github.iamnicknack.pjs.http.client.pwm

import io.github.iamnicknack.pjs.device.pwm.Pwm
import io.github.iamnicknack.pjs.device.pwm.PwmConfig
import io.github.iamnicknack.pjs.device.pwm.PwmProvider
import io.github.iamnicknack.pjs.http.pwm.PwmHandler
import kotlinx.coroutines.runBlocking

class HttpPwmProvider(
    private val pwmHandler: PwmHandler
) : PwmProvider {

    override fun create(config: PwmConfig): Pwm {
        val config = runBlocking { pwmHandler.createDevice(config.id, config.asPwmConfigPayload()) }
        return HttpPwm(pwmHandler, config as PwmConfig)
    }

    fun PwmConfig.asPwmConfigPayload() = PwmHandler.PwmConfigPayload(
        chip,
        channel,
        period,
        polarity,
        dutyCycle
    )
}