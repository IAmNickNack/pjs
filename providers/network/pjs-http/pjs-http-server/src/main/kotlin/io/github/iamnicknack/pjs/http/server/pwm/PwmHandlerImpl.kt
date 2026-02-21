package io.github.iamnicknack.pjs.http.server.pwm

import io.github.iamnicknack.pjs.device.pwm.Pwm
import io.github.iamnicknack.pjs.http.config.ConfigHandler
import io.github.iamnicknack.pjs.http.pwm.PwmHandler
import io.github.iamnicknack.pjs.http.server.deviceOrThrow
import io.github.iamnicknack.pjs.model.device.DeviceRegistry

class PwmHandlerImpl(
    private val deviceRegistry: DeviceRegistry,
    private val configHandler: ConfigHandler<Pwm>
) : PwmHandler, ConfigHandler<Pwm> by configHandler {

    override suspend fun on(deviceId: String) =
        deviceRegistry.deviceOrThrow<Pwm>(deviceId).on()

    override suspend fun off(deviceId: String) =
        deviceRegistry.deviceOrThrow<Pwm>(deviceId).off()

    override suspend fun setDutyCycle(deviceId: String, dutyCycle: Long) {
        deviceRegistry.deviceOrThrow<Pwm>(deviceId)
            .dutyCycle = dutyCycle
    }

    override suspend fun getDutyCycle(deviceId: String): Long =
        deviceRegistry.deviceOrThrow<Pwm>(deviceId)
            .dutyCycle

    override suspend fun setPolarity(deviceId: String, polarity: Pwm.Polarity) {
        deviceRegistry.deviceOrThrow<Pwm>(deviceId)
            .polarity = polarity
    }

    override suspend fun getPolarity(deviceId: String): Pwm.Polarity =
        deviceRegistry.deviceOrThrow<Pwm>(deviceId)
            .polarity

    override suspend fun setPeriod(deviceId: String, period: Long) {
        deviceRegistry.deviceOrThrow<Pwm>(deviceId)
            .period = period
    }

    override suspend fun getPeriod(deviceId: String): Long {
        return deviceRegistry.deviceOrThrow<Pwm>(deviceId)
            .period
    }
}