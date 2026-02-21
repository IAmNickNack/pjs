package io.github.iamnicknack.pjs.grpc

import io.github.iamnicknack.pjs.device.pwm.Pwm
import io.github.iamnicknack.pjs.device.pwm.PwmBean
import io.github.iamnicknack.pjs.device.pwm.PwmConfig
import io.github.iamnicknack.pjs.grpc.gen.v1.pwm.PwmConfigServiceGrpc
import io.github.iamnicknack.pjs.grpc.gen.v1.pwm.PwmServiceGrpc
import io.github.iamnicknack.pjs.model.device.DeviceConfig

class GrpcPwm(
    config: PwmConfig,
    private val stub : PwmServiceGrpc.PwmServiceBlockingStub,
    private val configStub: PwmConfigServiceGrpc.PwmConfigServiceBlockingStub,
) : Pwm, PwmBean(config) {

    override fun getConfig(): DeviceConfig<Pwm> {
        return this.config
    }

    override fun getDutyCycle(): Long {
        return stub.getDutyCycle(this.config.asDeviceRequest()).value
    }

    override fun setDutyCycle(dutyCycle: Long) {
        super.setDutyCycle(stub.setDutyCycle(this.config.asLongRequest(dutyCycle)).value)
    }

    override fun getPeriod(): Long {
        return stub.getPeriod(this.config.asDeviceRequest()).value
    }

    override fun setPeriod(period: Long) {
        super.setPeriod(stub.setPeriod(this.config.asLongRequest(period)).value)
    }

    override fun getPolarity(): Pwm.Polarity {
        return stub.getPolarity(this.config.asDeviceRequest()).polarity.asPolarity()
    }

    override fun setPolarity(polarity: Pwm.Polarity) {
        val polarityResponse = stub.setPolarity(this.config.asPolarityRequest(polarity.asPwmPolarity()))
        super.setPolarity(polarityResponse.polarity.asPolarity())
    }

    override fun on() {
        stub.on(this.config.asDeviceRequest())
    }

    override fun off() {
        stub.off(this.config.asDeviceRequest())
    }

    override fun close() {
        configStub.remove(config.asDeviceRequest())
    }
}