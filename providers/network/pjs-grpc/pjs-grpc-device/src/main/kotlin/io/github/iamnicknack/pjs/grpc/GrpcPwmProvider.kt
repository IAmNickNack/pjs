package io.github.iamnicknack.pjs.grpc

import io.github.iamnicknack.pjs.device.pwm.Pwm
import io.github.iamnicknack.pjs.device.pwm.PwmConfig
import io.github.iamnicknack.pjs.device.pwm.PwmProvider
import io.github.iamnicknack.pjs.grpc.gen.v1.pwm.PwmConfigServiceGrpc
import io.github.iamnicknack.pjs.grpc.gen.v1.pwm.PwmServiceGrpc
import io.grpc.Channel

class GrpcPwmProvider(channel: Channel) : PwmProvider {

    private val pwmStub = PwmServiceGrpc.newBlockingStub(channel)
    private val configStub = PwmConfigServiceGrpc.newBlockingStub(channel)

    override fun create(config: PwmConfig): Pwm {
        val created = configStub.create(config.asPwmConfigPayload())
        return GrpcPwm(created.asPwmConfig(), pwmStub, configStub)
    }
}