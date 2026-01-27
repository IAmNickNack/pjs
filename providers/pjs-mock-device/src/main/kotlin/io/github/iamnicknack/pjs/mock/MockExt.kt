package io.github.iamnicknack.pjs.mock

import io.github.iamnicknack.pjs.device.i2c.I2C
import io.github.iamnicknack.pjs.device.spi.Spi
import io.github.iamnicknack.pjs.logging.LoggingI2C
import io.github.iamnicknack.pjs.logging.LoggingSpi

val Spi.mockDelegate: MockSpi
    get() = (this as? LoggingSpi)
        ?.delegate as? MockSpi
        ?: error("LoggingSpi is not the delegate for this Spi")

val I2C.mockDelegate: MockI2C
    get() = (this as? LoggingI2C)
        ?.delegate as? MockI2C
        ?: error("LoggingI2C is not the delegate for this I2C")

/**
 * Expose [MockSpi.swapBuffers] on [Spi]
 */
fun Spi.swapBuffers() {
    mockDelegate.swapBuffers()
}

/**
 * Allow the [MockSpi.inBuffer] position to be reset on [Spi]
 */
fun Spi.rewindBuffer() {
    mockDelegate.inBuffer.position(0)
}

/**
 * Write data to the mock SPI device before swapping buffers and making the data available to read
 */
fun Spi.writeDataToRead(bytes: ByteArray): Spi {
    with(mockDelegate) {
        writeBytes(bytes)
        swapBuffers()
    }
    return this
}

/**
 * Write data to the mock SPI device before swapping buffers and making the data available to read
 */
fun Spi.writeToInputBuffer(vararg data: ByteArray) = with (mockDelegate) {
    inBuffer.mark()
    for (d in data) {
        inBuffer.put(d)
    }
    inBuffer.reset()
}

/**
 * Allow the [MockI2C.deviceBuffer] position to be reset on [I2C]
 */
fun I2C.rewindBuffer() {
    mockDelegate.deviceBuffer.position(0)
}


/**
 * Allow the position of the specified register to be reset on [I2C]
 */
fun I2C.rewindRegister(register: Int) {
    mockDelegate.getBuffer(register).position(0)
}