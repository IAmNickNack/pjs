package io.github.iamnicknack.pjs.grpc

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import io.github.iamnicknack.pjs.device.i2c.I2C
import io.github.iamnicknack.pjs.device.i2c.I2CConfig
import io.github.iamnicknack.pjs.device.i2c.impl.I2CRegister
import io.github.iamnicknack.pjs.device.i2c.impl.I2CSerialPort
import io.github.iamnicknack.pjs.mock.MockI2C
import io.github.iamnicknack.pjs.model.device.DeviceRegistry
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(PjsExtension::class)
class GrpcI2CTest {

    private val config = I2CConfig.builder()
        .id("test-bus")
        .bus(1)
        .build()

    @Test
    fun `can create device`(@PjsExtension.Local registry: DeviceRegistry) {
        val device = registry.create(config)
        assertThat(device.config).isEqualTo(config)
    }

    @Test
    fun `can find device`(
        @PjsExtension.Local localRegistry: DeviceRegistry,
        @PjsExtension.Remote remoteRegistry: DeviceRegistry
    ) {
        localRegistry.create(config)
        val localDevice = localRegistry.device<I2C>(config.id) ?: fail("cannot find local device")
        val remoteDevice = remoteRegistry.device<I2C>(config.id) ?: fail("cannot find remote device")

        assertThat(remoteDevice.config).isEqualTo(config)
        assertThat(localDevice.config).isEqualTo(config)
    }

    @Test
    fun `can write and read bytes`(
        @PjsExtension.Local localRegistry: DeviceRegistry,
        @PjsExtension.Remote remoteRegistry: DeviceRegistry
    ) {
        val localDevice = I2CSerialPort(0x20, localRegistry.create(config))
        val remoteDevice = remoteRegistry.device<I2C>(config.id) as? MockI2C
            ?: fail("cannot find mock device")
        val bytes: ByteArray = byteArrayOf(42, 84)

        localDevice.writeBytes(bytes)

        remoteDevice.deviceBuffer.flip()
        assertThat(remoteDevice.deviceBuffer.get()).isEqualTo(bytes[0])
        assertThat(remoteDevice.deviceBuffer.get()).isEqualTo(bytes[1])

        remoteDevice.deviceBuffer.flip()
        val readBytes = localDevice.readBytes(2)
        assertThat(readBytes).containsExactly(*bytes)
    }


    @Test
    fun `can write and read register`(
        @PjsExtension.Local localRegistry: DeviceRegistry,
        @PjsExtension.Remote remoteRegistry: DeviceRegistry
    ) {
        val localDevice = I2CRegister(0x20, 1, localRegistry.create(config))
        val remoteDevice = remoteRegistry.device<I2C>(config.id) as? MockI2C
            ?: fail("cannot find mock device")

        val bytes: ByteArray = byteArrayOf(42, 84)
        localDevice.writeBytes(bytes)

        val buffer = remoteDevice.getBuffer(1)
        buffer.flip()
        assertThat(buffer.get()).isEqualTo(42)
        assertThat(buffer.get()).isEqualTo(84)

        buffer.flip()
        val bytesRead = localDevice.readBytes(2)
        assertThat(bytesRead).containsExactly(*bytes)
    }
}