## Serial Ports - Multi-byte operations

A `SerialPort` is an extension of `Port<Int>` that adds support for multibyte operations.

PJS `SPI` and `I2C` implementations provide further specialisations of `SerialPort`.

* The SPI protocol allows for reading and writing of raw data. Once the device is configured, arbitrary bytes
  can be written to the device and read back. SPI peripherals tend to use the protocol as a transport layer,
  upon which more complex protocols can be implemented.
* I2C differs in in a number of ways. Most notably, the protocol is half-duplex and required to take peripheral
  addressing into account. As such, an I2C device cannot be used to read arbitrary bytes from the bus.
  Data needs to be packaged in a way which allows a data direction and address to be taken into account.

### SPI

The examples below use the `MockSpi` device. This provides additional operations to allow the mock buffers to be
manipulated. These operations are useful for testing and debugging SPI devices, but are not intended for use in
production code.

As with other `Serial` devices, `Spi` devices allow a series of bytes to be read or written:

```kotlin
val deviceRegistry = MockDeviceRegistry()
val loggingDeviceRegistry = LoggingDeviceRegistry(deviceRegistry)
val spi: Spi = loggingDeviceRegistry.create(SpiConfig.builder().bus(0).chipSelect(0).build())

/* Mock data to be read later */
spi.writeDataToRead(byteArrayOf(0x01, 0x02, 0x03))

/**
 * Read multiple bytes
 */
val data = spi.readBytes(3)
notebookLogger.info("Read bytes: {}", data.contentToString())
```
```shell
INFO  [i.g.i.p.l.LoggingDeviceRegistry     ] Created LoggingSpi device with id: SPI-0.0
DEBUG [device.MockSpi::SPI-0.0             ] Sending : 3 bytes: [00 00 00]
DEBUG [device.MockSpi::SPI-0.0             ] Received: 3 bytes: [01 02 03]
INFO  [notebook                            ] Read bytes: [1, 2, 3]
```

The above example is functionally similar to passing a pre-allocated buffer to `readBytes`:

```kotlin
/* Reset the mock to allow bytes to be re-read */
spi.rewindBuffer()

/**
 * Read bytes into the provided buffer
 */
val data = ByteArray(3)
spi.readBytes(data)
notebookLogger.info("Read bytes into buffer: {}", data.contentToString())
```
```shell
DEBUG [device.MockSpi::SPI-0.0             ] Sending : 3 bytes: [00 00 00]
DEBUG [device.MockSpi::SPI-0.0             ] Received: 3 bytes: [01 02 03]
INFO  [notebook                            ] Read bytes into buffer: [1, 2, 3]
```
Because `Spi` is a `Serial` device, it can also be used with an `OutputStream` to write bytes in a streaming manner:

```kotlin
spi.outputStream.use {
    val bytes = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06)

    it.write(bytes, 0, 3)
    it.write(bytes, 3, 3)
}
```
```shell
DEBUG [device.MockSpi::SPI-0.0             ] Sending : 3 bytes: [01 02 03]
DEBUG [device.MockSpi::SPI-0.0             ] Received: 3 bytes: [00 00 00]
DEBUG [device.MockSpi::SPI-0.0             ] Sending : 3 bytes: [04 05 06]
DEBUG [device.MockSpi::SPI-0.0             ] Received: 3 bytes: [00 00 00]
```

### SPI Transfer

`SpiTransfer`s allow for multiple operations to be performed in a single transaction. The API
design reflects the underlying SPI protocol, as implemented on Linux.

Transfers can be useful when devices require multiple logical steps to complete a single operation.
Cognitively, they allow the developer to think about the steps involved in performing a single operation.
From a hardware perspective, they allow applications to take full advantage of functionality provided by
the system, including introducing delays between operations and control over the chip select pin.

For many cases, these could be reduced to a single operation. In fact, the `DefaultSpiTransfer` implementation
does this as far as is possible by building a composite message which shares single read and
write buffers.

The example below illustrates how a transfer operation can be used to perform the _write-then-read_ sequence
required to read a value from a MCP23S08 register:

* The first message contains the read register opcode.
* The second message contains the register index.
* The third message contains the read buffer.

```kotlin
val spiTransfer = LoggingSpiTransfer(DefaultSpiTransfer(spi), spi.config.id)
val readBuffer = ByteArray(1)

// set 0x10 as the value of the mock register
spi.writeToInputBuffer(byteArrayOf(0, 0, 0x10))

spiTransfer.transfer(
    // Specify operation type (read register)
    SpiTransfer.Message.write(byteArrayOf(0x41)),
    // Specify register index
    SpiTransfer.Message.write(byteArrayOf(0x09)),
    // Read the contents of the register
    SpiTransfer.Message.read(readBuffer)
)
notebookLogger.info("Read buffer: ${readBuffer.contentToString()}")
```
```shell
DEBUG [device.DefaultSpiTransfer::SPI-0.0  ] [>[41], >[09], >[00]]
DEBUG [device.MockSpi::SPI-0.0             ] Sending : 3 bytes: [41 09 00]
DEBUG [device.MockSpi::SPI-0.0             ] Received: 3 bytes: [00 00 10]
DEBUG [device.DefaultSpiTransfer::SPI-0.0  ] [<[00], <[00], <[10]]
```

A `Message` can contain additional metadata about the operation that can be used by the Linux SPI
implementation to control the transfer operation. Leveraging this functionality allows a single transfer
to perform multiple logical operations.

The following example shows how a single transfer operation can take advantage of this to perform multiple
_write-then-read_ operations in a single transaction.

Note, however, that the `DefaultSpiTransfer` implementation fakes the chip-select functionality by executing
multiple `Spi`-level transfers in sequence. This can differ by `SpiTransfer` implementation.
`NativeSpiTransfer` does not delegate to `Spi` and calls `ioctl` directly, allowing it to take
advantage of the chip-select functionality provided by the underlying Linux SPI implementation.

```kotlin
spi.writeToInputBuffer(byteArrayOf(0, 0, 0x10), byteArrayOf(0, 0, 0x20))

val spiTransfer = DefaultSpiTransfer(spi)
val readBuffer1 = ByteArray(1)
val readBuffer2 = ByteArray(1)
spiTransfer.transfer(
    SpiTransfer.Message.write(byteArrayOf(0x41)),
    SpiTransfer.Message.write(byteArrayOf(0x01)),
    SpiTransfer.DefaultMessage(ByteArray(1), 0, readBuffer1, 0, 1, 0, true),
    SpiTransfer.Message.write(byteArrayOf(0x41)),
    SpiTransfer.Message.write(byteArrayOf(0x09)),
    SpiTransfer.DefaultMessage(ByteArray(1), 0, readBuffer2, 0, 1, 0, true)
)
readBuffer1 + readBuffer2
```
```shell
DEBUG [device.MockSpi::SPI-0.0             ] Sending : 3 bytes: [41 01 00]
DEBUG [device.MockSpi::SPI-0.0             ] Received: 3 bytes: [00 00 10]
DEBUG [device.MockSpi::SPI-0.0             ] Sending : 3 bytes: [41 09 00]
DEBUG [device.MockSpi::SPI-0.0             ] Received: 3 bytes: [00 00 20]
```

### SPI Device Implementation

Specific device implementations are boyond the scope of the core APIs. However, the core APIs aim to provide
the functionality required to implement these devices as required.

For example, in addition to the [MCP23S08](../pjs-sandbox/src/main/kotlin/io/github/iamnicknack/pjs/sandbox/device/mcp/Mcp23x08.kt)-type
device described above, [Microchip25LcEeprom](../pjs-sandbox/src/main/kotlin/io/github/iamnicknack/pjs/sandbox/device/eeprom/Microchip25LcEeprom.kt)
provides an example a slightly more complex SPI device implementation, again built on top of the core SPI APIs.

What can become clear when implementing more complex devices is that the generic nature of underlying SPI and
i2c protocols makes generic mock implementations of the hardware layer difficult to implement. The alternative
is to provide a mock implementation which specifically emulates the transport for the device under test.

[MockLc25SpiTransfer](../pjs-sandbox/src/main/kotlin/io/github/iamnicknack/pjs/sandbox/device/eeprom/MockLc25SpiTransfer.kt)
is an example of a device-specific mock.

```kotlin
val eepromHoldPin = LoggingGpioPort(MockGpioPort(GpioPortConfig.builder().mode(GpioPortMode.OUTPUT).pin(10).build())).pin()
val mockTransfer = LoggingSpiTransfer(MockLc25SpiTransfer(8096, 32), "eeprom")
val eeprom = Microchip25LcEeprom(mockTransfer, eepromHoldPin)
val address = 0x1234

notebookLogger.info("Read/write at address $address")
eeprom.write(address, byteArrayOf(1, 2, 3))
val bytes = eeprom.read(address, 3)
notebookLogger.info("Read EEPROM data: {}", bytes.contentToString())
```
```shell
DEBUG [device.MockLc25SpiTransfer::eeprom  ] [>[04]]
INFO  [i.g.i.p.s.d.e.MockLc25SpiTransfer   ] Write disable: None
DEBUG [device.MockLc25SpiTransfer::eeprom  ] [<[00]]
DEBUG [d.MockGpioPort::GPIO-OUTPUT-[10]    ] Writing port value: ---------------------0----------, 0
INFO  [notebook                            ] Read/write at address 4660
DEBUG [d.MockGpioPort::GPIO-OUTPUT-[10]    ] Writing port value: ---------------------1----------, 1
DEBUG [i.g.i.p.s.d.e.Microchip25LcEeprom   ] Writing to page at address 0x1234
DEBUG [device.MockLc25SpiTransfer::eeprom  ] [>[06]]
INFO  [i.g.i.p.s.d.e.MockLc25SpiTransfer   ] Write enable: WEL
DEBUG [device.MockLc25SpiTransfer::eeprom  ] [<[00]]
DEBUG [device.MockLc25SpiTransfer::eeprom  ] [>[02], >[12 34], >[01 02 03]]
INFO  [i.g.i.p.s.d.e.MockLc25SpiTransfer   ] Write operation
DEBUG [device.MockLc25SpiTransfer::eeprom  ] [<[00], <[00 00], <[00 00 00]]
DEBUG [device.MockLc25SpiTransfer::eeprom  ] [>[05], >[00]]
INFO  [i.g.i.p.s.d.e.MockLc25SpiTransfer   ] Read status register: WEL
DEBUG [device.MockLc25SpiTransfer::eeprom  ] [<[00], <[00]]
DEBUG [device.MockLc25SpiTransfer::eeprom  ] [>[04]]
INFO  [i.g.i.p.s.d.e.MockLc25SpiTransfer   ] Write disable: None
DEBUG [device.MockLc25SpiTransfer::eeprom  ] [<[00]]
DEBUG [d.MockGpioPort::GPIO-OUTPUT-[10]    ] Writing port value: ---------------------0----------, 0
DEBUG [d.MockGpioPort::GPIO-OUTPUT-[10]    ] Writing port value: ---------------------1----------, 1
DEBUG [device.MockLc25SpiTransfer::eeprom  ] [>[03], >[12 34], >[00 00 00]]
INFO  [i.g.i.p.s.d.e.MockLc25SpiTransfer   ] Read operation
DEBUG [device.MockLc25SpiTransfer::eeprom  ] [<[00], <[00 00], <[01 02 03]]
DEBUG [d.MockGpioPort::GPIO-OUTPUT-[10]    ] Writing port value: ---------------------0----------, 0
INFO  [notebook                            ] Read EEPROM data: [1, 2, 3]
```

### I2C

An I2C instance creates an instance of the I2C bus that represents the I2C capability of the hardware and is again
exposed at its lowest level as a message-based API. Device implementations can use this API to communicate with
the hardware while presenting a more appropriate, domain-specific API to the rest of the application.

To send an I2C message via the bus instance, messages must be created and passed to the `transfer` method.

Each message contains:
- Address: The 7-bit I2C address of the device to communicate with.
- Data: The byte array containing the data to be written or read.
- Offset: The starting index within the data array for the transfer.
- Length: The number of bytes to transfer.
- Type: Whether the message is a write or read operation.

```kotlin
val i2c = loggingDeviceRegistry.create(I2CConfig.builder().bus(0).build())
var bytes = ByteArray(3) { it.toByte() }
i2c.transfer(I2C.Message.write(0x20, bytes, 0, 3))

i2c.rewindBuffer()

bytes = ByteArray(3)
i2c.transfer(I2C.Message.read(0x20, bytes, 0, 3))

notebookLogger.info("Read bytes: {}", bytes.contentToString())
```
```shell
INFO  [i.g.i.pjs.mock.MockDeviceRegistry   ] Created MockI2C device with id: I2CBus-0
INFO  [i.g.i.p.l.LoggingDeviceRegistry     ] Created LoggingI2C device with id: I2CBus-0
DEBUG [device.MockI2C::I2CBus-0            ] [>0x20: [00 01 02]]
DEBUG [device.MockI2C::I2CBus-0            ] [<0x20: [00 01 02]]
INFO  [notebook                            ] Read bytes: [0, 1, 2]
```
#### Basic I2C SerialPort

It's probably not desirable to work with the bus device directly. Instead, a higher-level abstraction can be used to simplify I2C communication.

[I2CSerialPort](../pjs-core/src/main/kotlin/io/github/iamnicknack/pjs/device/i2c/impl/I2CSerialPort.kt) provides a basic `SerialPort`
implementation that allows writing and reading bytes to and from an addressed device the bus.

```kotlin

val i2c = loggingDeviceRegistry.create(I2CConfig.builder().bus(0).build())
val i2cPort: SerialPort = I2CSerialPort(0x20, i2c)
i2cPort.write(0x10)

i2c.rewindBuffer()

val value = i2cPort.read()

notebookLogger.info("Read value: {}", value)
```
```shell
INFO  [i.g.i.pjs.mock.MockDeviceRegistry   ] Created MockI2C device with id: I2CBus-0
INFO  [i.g.i.p.l.LoggingDeviceRegistry     ] Created LoggingI2C device with id: I2CBus-0
DEBUG [device.MockI2C::I2CBus-0            ] [>0x20: [10]]
DEBUG [device.MockI2C::I2CBus-0            ] [<0x20: [10]]
INFO  [notebook                            ] Read value: 16
```

#### Basic I2C Register

I2C devices often expose individual registers which it would be cumbersome to work with directly,
either via the bus device or the `I2CSerialPort` abstraction.
It would be much more convenient to be able to write and read values from a register directly.

The [I2CRegister](../pjs-core/src/main/kotlin/io/github/iamnicknack/pjs/device/i2c/impl/I2CRegister.kt) provides a
higher-level abstraction for working with individual registers on an I2C bus.

Given a device and register address, the register can be written to and read from as a `SerialPort`:

```kotlin
val deviceAddress = 0x20
val deviceRegister = 0x09

val i2c = loggingDeviceRegistry.create(I2CConfig.builder().bus(0).build())
val i2cRegister = I2CRegister(deviceAddress, deviceRegister, i2c)

i2cRegister.write(0x10)

i2c.rewindRegister(deviceRegister)

val value = i2cRegister.read()
notebookLogger.info("Read value: {}", value)
```
```shell
INFO  [i.g.i.pjs.mock.MockDeviceRegistry   ] Created MockI2C device with id: I2CBus-0
INFO  [i.g.i.p.l.LoggingDeviceRegistry     ] Created LoggingI2C device with id: I2CBus-0
DEBUG [device.MockI2C::I2CBus-0            ] [>0x20: [09], >0x20: [10]]
DEBUG [device.MockI2C::I2CBus-0            ] [>0x20: [09], <0x20: [10]]
INFO  [notebook                            ] Read value: 16
```
```kotlin
i2c.rewindRegister(deviceRegister) // reposition the mock i2c buffer to 0
i2cRegister.writeBytes(byteArrayOf(0x10, 0x20, 0x30))

i2c.rewindRegister(deviceRegister) // reposition the mock i2c buffer to 0
val bytes = ByteArray(3)
i2cRegister.readBytes(bytes)
notebookLogger.info("Read bytes: {}", bytes.contentToString())
```
```shell
INFO  [i.g.i.pjs.mock.MockDeviceRegistry   ] Created MockI2C device with id: I2CBus-0
INFO  [i.g.i.p.l.LoggingDeviceRegistry     ] Created LoggingI2C device with id: I2CBus-0
DEBUG [device.MockI2C::I2CBus-0            ] [>0x20: [09], >0x20: [10 20 30]]
DEBUG [device.MockI2C::I2CBus-0            ] [>0x20: [09], <0x20: [10 20 30]]
INFO  [notebook                            ] Read bytes: [16, 32, 48]
```

#### I2C OLED Display

The SH1106 OLED display is an example of a device which can make use of the `I2CSerialPort` abstraction. Its
register-like inputs don't behave the same as the registers in, for example, the MCP23008. Rather, the 'register'
is provided as the first byte in a continuous sequence of either command or data bytes.

The sandbox [OLED device](../pjs-sandbox/pjs-device-sh1106) takes advantage ot the `SerialPort`'s ability to
provide a Java `BufferedOutputStream`. First the prefix byte is written, followed by the 'payload' bytes. The
`BufferedOutputStream` is left to write the complete stream of bytes to the `SerialPort`'s `OutputStream`.

This is essentially a bespoke register implementation. It provides register-like behaviour, but within the
constraints of the protocol defined in the datasheet of the device.

```kotlin
val i2c = deviceRegistry.create(I2CConfig.builder().bus(0).build())

// default device operations
val oledOperations = Sh1106Operations(Sh1106Driver(LoggingI2C(i2c, 32), 0x3c))

// alternative logging device to allow longer byte arrays in messages
val loggingOperations = Sh1106Operations(Sh1106Driver(LoggingI2C(i2c, 128), 0x3c))

oledOperations.init()
oledOperations.clear()
oledOperations.displayOn();
loggingOperations.drawText("Hello World!")
```
```shell
INFO  [i.g.i.pjs.mock.MockDeviceRegistry   ] Created MockI2C device with id: I2CBus-0
DEBUG [device.MockI2C::I2CBus-0            ] [>0x3c: [00 AE D5 80 A8 3F D3 00 40 AD 8B A1 C8 DA 12 81 FF D9 1F DB 40 33 A6 A4]]
DEBUG [device.MockI2C::I2CBus-0            ] [>0x3c: [00 B0 10 02]]
DEBUG [device.MockI2C::I2CBus-0            ] [>0x3c: [40 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00]]
DEBUG [device.MockI2C::I2CBus-0            ] [>0x3c: [00 B1 10 02]]
DEBUG [device.MockI2C::I2CBus-0            ] [>0x3c: [40 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00]]
DEBUG [device.MockI2C::I2CBus-0            ] [>0x3c: [00 B2 10 02]]
DEBUG [device.MockI2C::I2CBus-0            ] [>0x3c: [40 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00]]
DEBUG [device.MockI2C::I2CBus-0            ] [>0x3c: [00 B3 10 02]]
DEBUG [device.MockI2C::I2CBus-0            ] [>0x3c: [40 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00]]
DEBUG [device.MockI2C::I2CBus-0            ] [>0x3c: [00 B4 10 02]]
DEBUG [device.MockI2C::I2CBus-0            ] [>0x3c: [40 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00]]
DEBUG [device.MockI2C::I2CBus-0            ] [>0x3c: [00 B5 10 02]]
DEBUG [device.MockI2C::I2CBus-0            ] [>0x3c: [40 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00]]
DEBUG [device.MockI2C::I2CBus-0            ] [>0x3c: [00 B6 10 02]]
DEBUG [device.MockI2C::I2CBus-0            ] [>0x3c: [40 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00]]
DEBUG [device.MockI2C::I2CBus-0            ] [>0x3c: [00 B7 10 02]]
DEBUG [device.MockI2C::I2CBus-0            ] [>0x3c: [40 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00]]
DEBUG [device.MockI2C::I2CBus-0            ] [>0x3c: [00 AF]]
DEBUG [device.MockI2C::I2CBus-0            ] [>0x3c: [40 7F 08 08 08 7F 00 38 54 54 54 18 00 00 41 7F 40 00 00 00 41 7F 40 00 00 38 44 44 44 38 00 00 00 00 00 00 00 3F 40 38 40 3F 00 38 44 44 44 38 00 7C 08 04 04 08 00 00 41 7F 40 00 00 38 44 44 28 7F 00 00 00 5F 00 00 00]]
```