package io.github.iamnicknack.pjs.krpc

import io.github.iamnicknack.pjs.device.pwm.Pwm
import io.github.iamnicknack.pjs.device.pwm.PwmBean
import io.github.iamnicknack.pjs.device.pwm.PwmConfig
import io.github.iamnicknack.pjs.grpc.gen.v1.pwm.PwmConfigService
import io.github.iamnicknack.pjs.grpc.gen.v1.pwm.PwmService
import io.github.iamnicknack.pjs.krpc.proto.asDeviceRequest
import io.github.iamnicknack.pjs.krpc.proto.asLongRequest
import io.github.iamnicknack.pjs.krpc.proto.asPolarity
import io.github.iamnicknack.pjs.krpc.proto.asPolarityRequest
import io.github.iamnicknack.pjs.krpc.proto.asPwmPolarity
import io.github.iamnicknack.pjs.model.device.DeviceConfig
import kotlinx.coroutines.runBlocking

class KrpcPwm(
    config: PwmConfig,
    private val pwmService: PwmService,
    private val pwmConfigService: PwmConfigService
) : Pwm, PwmBean(config) {

    override fun getConfig(): DeviceConfig<Pwm> = config

    override fun setDutyCycle(dutyCycle: Long): Unit = runBlocking {
        super.dutyCycle = pwmService.setDutyCycle(config.asLongRequest(dutyCycle)).value
    }

    override fun getDutyCycle(): Long = runBlocking {
        pwmService.getDutyCycle(config.asDeviceRequest()).value
    }

    override fun setPeriod(period: Long): Unit = runBlocking {
        super.setPeriod(pwmService.setPeriod(config.asLongRequest(period)).value)
    }

    override fun getPeriod(): Long = runBlocking {
        pwmService.getPeriod(config.asDeviceRequest()).value
    }

    override fun setPolarity(polarity: Pwm.Polarity): Unit = runBlocking {
        val polarityResponse = pwmService.setPolarity(config.asPolarityRequest(polarity.asPwmPolarity()))
        super.setPolarity(polarityResponse.polarity.asPolarity())
    }

    override fun getPolarity(): Pwm.Polarity = runBlocking {
        pwmService.getPolarity(config.asDeviceRequest()).polarity.asPolarity()
    }

    override fun on(): Unit = runBlocking {
        pwmService.on(config.asDeviceRequest())
    }

    override fun off(): Unit = runBlocking {
        pwmService.off(config.asDeviceRequest())
    }

    override fun close(): Unit = runBlocking {
        pwmConfigService.remove(config.asDeviceRequest())
    }
}