package io.github.iamnicknack.pjs.http.client

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
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HttpDeviceRegistryTest {

    @TestFactory
    fun `can create and remove devices`(): List<DynamicTest> {
        val id = "test-device"

        return listOf<Pair<Class<*>, (DeviceRegistry) -> Unit>>(
            GpioPort::class.java to { it.create(GpioPortConfig.builder().id(id).mode(GpioPortMode.OUTPUT).pin(1).build()) },
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
}