package io.github.iamnicknack.pjs.http.client.pwm

import io.github.iamnicknack.pjs.device.pwm.Pwm
import io.github.iamnicknack.pjs.device.pwm.PwmConfig
import io.github.iamnicknack.pjs.http.pwm.PwmHandler
import io.github.iamnicknack.pjs.model.device.DeviceConfig
import kotlinx.coroutines.runBlocking

class HttpPwm(
    private val pwmHandler: PwmHandler,
    private val config: PwmConfig
) : Pwm {

    private var isEnabled: Boolean = false

    override fun setDutyCycle(dutyCycle: Int) = runBlocking { pwmHandler.setDutyCycle(config.id, dutyCycle) }

    override fun getDutyCycle(): Int = runBlocking { pwmHandler.getDutyCycle(config.id) }

    override fun setFrequency(frequency: Int) = runBlocking { pwmHandler.setFrequency(config.id, frequency) }

    override fun getFrequency(): Int = runBlocking { pwmHandler.getFrequency(config.id) }

    override fun setPolarity(polarity: Pwm.Polarity) = runBlocking { pwmHandler.setPolarity(config.id, polarity) }

    override fun getPolarity(): Pwm.Polarity = runBlocking { pwmHandler.getPolarity(config.id) }

    override fun setEnabled(enabled: Boolean) {
        if (enabled) on() else off()
    }

    override fun isEnabled(): Boolean = isEnabled

    override fun on() {
        runBlocking { pwmHandler.on(config.id) }
        isEnabled = true
    }

    override fun off() {
        runBlocking { pwmHandler.off(config.id) }
        isEnabled = false
    }

    override fun read(): Boolean = isEnabled

    override fun write(value: Boolean) {
        if (value) on() else off()
    }

    override fun getConfig(): DeviceConfig<Pwm> = config

    override fun close() = runBlocking {
        pwmHandler.removeDevice(config.id)
    }
}