package io.github.iamnicknack.pjs.grpc

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import io.github.iamnicknack.pjs.device.pwm.Pwm
import io.github.iamnicknack.pjs.device.pwm.PwmConfig
import io.github.iamnicknack.pjs.model.device.DeviceRegistry
import io.github.iamnicknack.pjs.mock.MockPwm
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(PjsExtension::class)
class GrpcPwmTest {

    private val config = PwmConfig.builder()
        .frequency(220)
        .dutyCycle(25)
        .id("test")
        .build()

    @Test
    fun `can create`(
        @PjsExtension.Local localRegistry: DeviceRegistry,
        @PjsExtension.Remote remoteRegistry: DeviceRegistry
    ) {
        val device = localRegistry.create(config)
        assertThat(device.config.id).isEqualTo(config.id)
        assertThat(device.dutyCycle).isEqualTo(config.dutyCycle)
        assertThat(device.frequency).isEqualTo(config.frequency)

        val remoteDevice = remoteRegistry.device<Pwm>(config.id) as? MockPwm ?: fail("cannot find device")
        assertThat(remoteDevice).isNotNull()
    }

    @Test
    fun `can set duty cycle`(@PjsExtension.Local registry: DeviceRegistry) {
        val device = registry.create(config)
        assertThat(device.dutyCycle).isEqualTo(25)
        device.dutyCycle = 75
        assertThat(device.dutyCycle).isEqualTo(75)
    }

    @Test
    fun `can set frequency`(@PjsExtension.Local registry: DeviceRegistry) {
        val device = registry.create(config)
        assertThat(device.frequency).isEqualTo(220)
        device.frequency = 100
        assertThat(device.frequency).isEqualTo(100)
    }

    @Test
    fun `can enable`(
        @PjsExtension.Local localRegistry: DeviceRegistry,
        @PjsExtension.Remote remoteRegistry: DeviceRegistry
    ) {
        val device = localRegistry.create(config)
        val remoteDevice = remoteRegistry.device<Pwm>(config.id) as? MockPwm ?: fail("cannot find device")

        device.on()
        assertThat(remoteDevice.read()).isTrue()

        device.off()
        assertThat(remoteDevice.read()).isFalse()
    }
}