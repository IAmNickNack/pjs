package io.github.iamnicknack.pjs.grpc

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import io.github.iamnicknack.pjs.device.spi.Spi
import io.github.iamnicknack.pjs.device.spi.SpiConfig
import io.github.iamnicknack.pjs.device.spi.SpiProvider
import io.github.iamnicknack.pjs.device.spi.SpiTransfer
import io.github.iamnicknack.pjs.mock.MockSpi
import io.github.iamnicknack.pjs.model.device.DeviceRegistry
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(PjsExtension::class)
class GrpcSpiTest {

    private val config = SpiConfig.builder()
        .bus(1)
        .baudRate(100_000)
        .build()

    @Test
    fun `can create`(
        @PjsExtension.Local localRegistry: DeviceRegistry,
        @PjsExtension.Remote remoteRegistry: DeviceRegistry
    ) {
        val localDevice = localRegistry.create(config)
        assertThat(localDevice.config).isEqualTo(config)

        val remoteDevice = remoteRegistry.device<Spi>(config.id) ?: fail("cannot find device")
        assertThat(remoteDevice.config).isEqualTo(config)
    }

    @Test
    fun `can write and read`(
        @PjsExtension.Local localRegistry: DeviceRegistry,
        @PjsExtension.Remote remoteRegistry: DeviceRegistry
    ) {
        val localDevice = localRegistry.create(config)
        localDevice.write(42)

        val remoteDevice = remoteRegistry.device<Spi>(config.id) as? MockSpi ?: fail("cannot find device")
        assertThat(remoteDevice.outBuffer[0]).isEqualTo(42)

        remoteDevice.swapBuffers()
        assertThat(localDevice.read()).isEqualTo(42)
    }

    @Test
    fun `can transfer`(
        @PjsExtension.Local localRegistry: DeviceRegistry,
        @PjsExtension.Remote remoteRegistry: DeviceRegistry
    ) {
        val localDevice = localRegistry.create(config)
        val localProvider = localRegistry.getProvider(SpiConfig::class.java) as SpiProvider

        val transfer = localProvider.createTransfer(localDevice)
        assertThat(transfer).isNotNull()

        val remoteDevice = remoteRegistry.device<Spi>(config.id) as? MockSpi ?: fail("cannot find device")
        remoteDevice.write(42)
        remoteDevice.swapBuffers()

        val message = SpiTransfer.Message.read(ByteArray(1) { 0 })
        transfer.transfer(message)
        assertThat(message.read()[0]).isEqualTo(42)
    }
}