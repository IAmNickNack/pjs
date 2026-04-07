package io.github.iamnicknack.pjs.http.client

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import io.github.iamnicknack.pjs.device.gpio.GpioPort
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig
import io.github.iamnicknack.pjs.device.gpio.GpioPortMode
import io.github.iamnicknack.pjs.device.i2c.I2C
import io.github.iamnicknack.pjs.device.i2c.I2CConfig
import io.github.iamnicknack.pjs.device.pwm.PwmConfig
import io.github.iamnicknack.pjs.device.spi.Spi
import io.github.iamnicknack.pjs.device.spi.SpiConfig
import io.github.iamnicknack.pjs.http.pjsHttpTestCase
import io.github.iamnicknack.pjs.model.device.DeviceRegistry
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HttpDeviceRegistryTest {

    @TestFactory
    fun `can create and remove devices`(): List<DynamicTest> {
        val id = "test-device"

        return listOf<Pair<Class<*>, (DeviceRegistry) -> Unit>>(
            GpioPort::class.java to { it.create(GpioPortConfig.builder().id(id).portMode(GpioPortMode.OUTPUT).pin(1).build()) },
            Spi::class.java to { it.create(SpiConfig.builder().id(id).build()) },
            I2C::class.java to { it.create(I2CConfig.builder().id(id).build()) },
            PwmConfig::class.java to { it.create(PwmConfig.builder().id(id).build()) }
        ).map { (type, consumer) ->
            DynamicTest.dynamicTest(type.simpleName) {
                pjsHttpTestCase {
                    consumer(httpDeviceRegistry)
                    assertTrue { mockDeviceRegistry.contains(id) }
                    httpDeviceRegistry.close()
                    assertFalse { mockDeviceRegistry.contains(id) }
                }
            }
        }
    }

    @Test
    fun `can lazily create a device`() = pjsHttpTestCase {
        val remoteConfig = GpioPortConfig.builder()
            .id("test-port")
            .portMode(GpioPortMode.OUTPUT)
            .pin(1)
            .build()

        // create the device in the remote registry
        mockDeviceRegistry.create(remoteConfig)
        // fetch the device from the local registry
        val localDevice = httpDeviceRegistry.device(remoteConfig.id, GpioPort::class.java)
        val localConfig = localDevice?.config as? GpioPortConfig
        assertThat(localConfig).isEqualTo(remoteConfig)

        // remove the device from the local registry
        httpDeviceRegistry.remove(remoteConfig.id)
        assertThat(httpDeviceRegistry.contains(remoteConfig.id)).isFalse()

        // assert that the device still exists in the remote registry
        assertThat(mockDeviceRegistry.device(remoteConfig.id, GpioPort::class.java)).isNotNull()
    }

    @Test
    fun `can lazily create a device in proxy mode`() = pjsHttpTestCase {
        val remoteConfig = GpioPortConfig.builder()
            .id("test-port")
            .portMode(GpioPortMode.OUTPUT)
            .pin(1)
            .build()

        // create the device in the remote registry
        mockDeviceRegistry.create(remoteConfig)

        // check that creating the device in the local registry fails
        assertThrows<RemoteDeviceException.RemoteDeviceConflictException> { httpDeviceRegistry.create(remoteConfig) }

        // create the device via the proxy
        val localDevice = httpProxyDeviceRegistry.create(remoteConfig)
        val localConfig = localDevice?.config as? GpioPortConfig
        assertThat(localConfig).isEqualTo(remoteConfig)

        // remove the device from the local registry
        httpDeviceRegistry.remove(remoteConfig.id)
        assertThat(httpDeviceRegistry.contains(remoteConfig.id)).isFalse()

        // assert that the device still exists in the remote registry
        assertThat(mockDeviceRegistry.device(remoteConfig.id, GpioPort::class.java)).isNotNull()
    }
}