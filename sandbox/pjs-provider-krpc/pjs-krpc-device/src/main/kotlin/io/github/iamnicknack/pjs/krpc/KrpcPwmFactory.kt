package io.github.iamnicknack.pjs.krpc

import io.github.iamnicknack.pjs.device.pwm.Pwm
import io.github.iamnicknack.pjs.device.pwm.PwmConfig
import io.github.iamnicknack.pjs.device.pwm.PwmFactory
import io.github.iamnicknack.pjs.krpc.gen.v1.pwm.PwmConfigService
import io.github.iamnicknack.pjs.krpc.gen.v1.pwm.PwmService
import io.github.iamnicknack.pjs.krpc.proto.asPwmConfig
import io.github.iamnicknack.pjs.krpc.proto.asPwmConfigPayload
import kotlinx.coroutines.runBlocking

class KrpcPwmFactory(
    private val pwmService: PwmService,
    private val pwmConfigService: PwmConfigService,
) : PwmFactory {

    override fun create(config: PwmConfig): Pwm = runBlocking {
        val created = pwmConfigService.create(config.asPwmConfigPayload()).asPwmConfig()
        KrpcPwm(created, pwmService, pwmConfigService)
    }
}