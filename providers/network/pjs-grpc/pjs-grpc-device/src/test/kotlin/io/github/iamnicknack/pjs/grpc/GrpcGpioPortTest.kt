package io.github.iamnicknack.pjs.grpc

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.github.iamnicknack.pjs.device.gpio.GpioEventMode
import io.github.iamnicknack.pjs.device.gpio.GpioPort
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig
import io.github.iamnicknack.pjs.device.gpio.GpioPortMode
import io.github.iamnicknack.pjs.mock.MockGpioPort
import io.github.iamnicknack.pjs.model.device.DeviceRegistry
import io.grpc.StatusRuntimeException
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@ExtendWith(PjsExtension::class)
class GrpcGpioPortTest {

    private val logger = LoggerFactory.getLogger(GrpcGpioPortTest::class.java)

    private val outputConfig = GpioPortConfig.builder()
        .id("test-output")
        .portMode(GpioPortMode.OUTPUT)
        .build()

    private val inputConfig = GpioPortConfig.builder()
        .id("test-input")
        .portMode(GpioPortMode.INPUT)
        .eventMode(GpioEventMode.BOTH)
        .build()

    private val errorPortConfig = GpioPortConfig.builder()
        .id("test-error")
        .portMode(GpioPortMode.OUTPUT)
        .build()


    @Test
    fun `can write and read`(
        @PjsExtension.Local localRegistry: DeviceRegistry,
        @PjsExtension.Remote remoteRegistry: DeviceRegistry,
    ) {
        val device = localRegistry.create(outputConfig)
        device.write(42)
        assertThat(device.read()).isEqualTo(42)

        val remoteDevice = remoteRegistry.device<GpioPort>(outputConfig.id) as? MockGpioPort
            ?: fail("cannot find device")
        assertThat(remoteDevice.read()).isEqualTo(42)
    }

    @Test
    fun `cannot recreate the same id`(
        @PjsExtension.Local localRegistry: DeviceRegistry,
    ) {
        localRegistry.create(outputConfig)

        assertThrows<StatusRuntimeException> {
            localRegistry.create(outputConfig)
        }
    }

    @Test
    fun `test events`(
        @PjsExtension.Local localRegistry: DeviceRegistry,
        @PjsExtension.Remote remoteRegistry: DeviceRegistry,
    ) {
        val device = localRegistry.create(inputConfig)
        val remoteDevice = remoteRegistry.device<GpioPort>(inputConfig.id) as? MockGpioPort
            ?: fail("cannot find device")

        val latch = CountDownLatch(1)
        device.addListener {
            logger.info("Test received event: {}", it)
            latch.countDown()
        }

        logger.info("Setting value")
        remoteDevice.mockValue(42)

        latch.await(1, TimeUnit.SECONDS)
    }

    @Test
    fun `test events 2`(
        @PjsExtension.Local localRegistry: DeviceRegistry,
        @PjsExtension.Remote remoteRegistry: DeviceRegistry,
    ) {
        val device = localRegistry.create(inputConfig)
        val remoteDevice = remoteRegistry.device<GpioPort>(inputConfig.id) as? MockGpioPort
            ?: fail("cannot find device")

        val latch = CountDownLatch(1)
        device.addListener {
            logger.info("Test received event: {}", it)
            latch.countDown()
        }

        val executor = Executors.newSingleThreadExecutor()
        try {
            val future = executor.submit {
                logger.info("Setting value")
                remoteDevice.mockValue(42)
            }

            future.get(5, TimeUnit.SECONDS)

            latch.await(10, TimeUnit.SECONDS)
        } finally {
            executor.shutdownNow()
        }
    }

    @Test
    fun `will return exceptions`(
        @PjsExtension.Local localRegistry: DeviceRegistry,
        @PjsExtension.Remote remoteRegistry: DeviceRegistry,
    ) {
        val device = localRegistry.create(errorPortConfig)
        val remoteDevice = remoteRegistry.device<GpioPort>(errorPortConfig.id) as MockGpioPort
        remoteDevice.setFailWith(RuntimeException("test exception"))
        val result = runCatching { device.read() }
        assertEquals(true, result.exceptionOrNull()?.message?.endsWith("test exception"))
    }
}
