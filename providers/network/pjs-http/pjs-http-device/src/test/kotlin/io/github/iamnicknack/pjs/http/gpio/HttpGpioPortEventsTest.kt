package io.github.iamnicknack.pjs.http.gpio

import io.github.iamnicknack.pjs.device.gpio.GpioPort
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig
import io.github.iamnicknack.pjs.device.gpio.GpioPortMode
import io.github.iamnicknack.pjs.http.client.gpio.HttpGpioPort
import io.github.iamnicknack.pjs.http.pjsHttpTestCase
import io.github.iamnicknack.pjs.http.server.deviceOrThrow
import io.github.iamnicknack.pjs.mock.MockGpioPort
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HttpGpioPortEventsTest {

    @Test
    fun `can listen for events`() = pjsHttpTestCase {
        val config = GpioPortConfig.builder()
            .id("test-port")
            .mode(GpioPortMode.INPUT)
            .pin(1)
            .build()
        val httpPort = httpDeviceRegistry.create(config) as HttpGpioPort
        val mockPort = mockDeviceRegistry.deviceOrThrow<GpioPort>("test-port") as MockGpioPort

        val latch = CountDownLatch(1)
        httpPort.addListener { latch.countDown() }
        assertEquals(1, mockPort.listenerCount)

        mockPort.mockValue(42)
        assertTrue { latch.await(2, TimeUnit.SECONDS) }

        httpPort.close()
        assertEquals(0, mockPort.listenerCount)
    }
}