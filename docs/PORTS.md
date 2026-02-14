# Ports, Pins and SerialPorts

## Port

A `Port` provides a generic method for reading and writing data to a device, while abstracting away 
the underlying transport mechanism.

For example:
* GPIO is a `Port`
* I2C _can be_ one or many `Port`s
* SPI _can be_ one or many `Port`s
* PWM can even be interpreted as a collection of ports which expose frequency, duty cycle, etc. 

## SerialPort

A `SerialPort` is an extension of this which allows reading and writing a series of values. 

> This nomenclature might be misleading and could be changed in the future.

## Pin

A `Pin` is a specific implementation of a `Port` which is only capable of reading and writing a Boolean value.

--- 

## Configure the `DeviceRegistry`

Although it's possible to create devices directly, it's more convenient to use a `DeviceRegistry` to manage the creation of devices.
Delegating to the device registry provides some benefits:

* It provides a single point of configuration for the application
* It provides a single point where the lifecycle of devices can be managed
* It abstracts device provider configurations
* It can be swapped out depending on the runtime requirement

```kotlin
var actualRegistry: DeviceRegistry = MockDeviceRegistry()
```

### Registry decoration by delegation

Devices created via the registry can be decorated with debug logging information, by wrapping the registry instance with a `LoggingRegistry`.

The logging registry wraps all `Device` instances with logging decorators which can emit log messages when the device
is operated on.

```kotlin
val deviceRegistry: DeviceRegistry = LoggingDeviceRegistry(actualRegistry)
```

## Create a pin device

The registry can now be used to create a device. Only device configuration needs to be passed to the registry. If an appropriate provider is configured, the registry will handle the construction of the device.

Here a `GpioPort` is created with an arbitrary `id`, mapped to hardware pin `0` and configured as an output:

```kotlin
val pinConfig: GpioPortConfig = GpioPortConfig.builder()
    .id("test-pin")
    .pin(0)
    .mode(GpioPortMode.OUTPUT)
    .build()

val pinPort: GpioPort = deviceRegistry.create(pinConfig)
```

```shell
INFO io.github.iamnicknack.pjs.mock.MockDeviceRegistry - Created MockGpioPort device with id: test-pin
INFO io.github.iamnicknack.pjs.logging.LoggingDeviceRegistry - Created LoggingGpioPort device with id: test-pin
```

Similarly to how this might be done with the low-level Linux APIs for GPIO `lines` (where a line is mapped to one or many `offset`s which represent the individual pins),
what's created above is a `Port` which is mapped to only a single pin.

As with all ports, numeric state can be read from and written to the port:

```kotlin
pinPort.read()
pinPort.write(1)
```
```shell
DEBUG device.MockGpioPort::test-pin - Reading port value: -------------------------------0, 0
DEBUG device.MockGpioPort::test-pin - Writing port value: -------------------------------1, 1
```

However, if all that is required is a pin which can be `high` or `low`, a `Pin` device can be created from the port:

```kotlin
val pin = pinPort.pin()
pin.high()
pin.low()
```
```shell
DEBUG device.MockGpioPort::test-pin - Writing port value: -------------------------------1, 1
DEBUG device.MockGpioPort::test-pin - Writing port value: -------------------------------0, 0
```

### More pins

Because a `Pin` is derived from a `Port`, it's possible to create a `Pin` which controls multiple GPIO pins at once.

For example, a `Port` can be created to enable two mutally exclusive peripherals, ensuring that only one can be active at any one time. One being enabled when the pin is `low` and the other when the pin is `high`.

This can be done by providing alternative values for `high` and `low`:

```kotlin
val pinConfig = GpioPortConfig.builder()
    .id("test-pins")
    .pin(14, 15)
    .mode(GpioPortMode.OUTPUT)
    .build()

val pin = deviceRegistry.create(pinConfig).pin(0b10, 0x01)

pin.high()
pin.low()
```
```shell
INFO io.github.iamnicknack.pjs.mock.MockDeviceRegistry - Created MockGpioPort device with id: test-pins
INFO io.github.iamnicknack.pjs.logging.LoggingDeviceRegistry - Created LoggingGpioPort device with id: test-pins
DEBUG device.MockGpioPort::test-pins - Writing port value: ----------------10--------------, 2
DEBUG device.MockGpioPort::test-pins - Writing port value: ----------------01--------------, 1
```

## Generic ports

### Pin mapping

The `GpioPort` used above is an extension of the more generic `Port<Integer>` type.

A `Port<T>` allows values of type `T` to be written to and read from the port. Because `T` is not constrained, it can be anything that is relevant in the context of your application.

Assuming a 3-to-8 decoder is connected to four output pins on the Raspberry Pi is being used to enable various peripheral and is connected as follows:

| Pi Pin | 3-to-8 Input Pin     |
|--------|----------------------|
| 14     | A                    |
| 15     | B                    |
| 18     | C                    |
| 23     | Enable - Active High |

and the enable-pin of each device is then connected to a decoder output:

| Peripheral  | Output Pin |
|-------------|------------|
| Eeprom      | 0          |
| MCP23S08    | 1          |
| 74LS595 (A) | 2          |
| 74LS595 (B) | 3          |

An `enum` could be created to represent the set of peripherals which could then be used to exclusively enable each
device:

```kotlin
/**
 * An enumeration of the enable-pin configurations required by the application
 */
enum class Peripheral(
    val pinMask: Int
) {
    NONE(0),            // active high enable pin (active high enable on bit 3, defaults to 0)
    EEPROM(0 or 8),     // enables output pin 0   (pi output bit 0 + enable on bit 3)
    MCP23S08(1 or 8),   // enables output pin 1   (pi output bit 1 + enable on bit 3)
    SHIFT_A(2 or 8),    // enables output pin 2   (pi output bit 0+1 + enable on bit 3)
    SHIFT_B(3 or 8);    // enables output pin 3   (pi output bit 0+1 + enable on bit 3)

    companion object {
        fun of(value: Int): Peripheral = when (value) {
            NONE.pinMask -> NONE
            EEPROM.pinMask -> EEPROM
            MCP23S08.pinMask -> MCP23S08
            SHIFT_A.pinMask -> SHIFT_A
            SHIFT_B.pinMask -> SHIFT_B
            else -> error("Failed to map value $value to peripheral")
        }
    }
}

/**
 * A [GpioPortConfig] for the pins required to control the decoder
 */
val portConfig = GpioPortConfig.builder()
    .id("3-to-8-enable-port")
    .pin(14, 15, 18, 23)
    .mode(GpioPortMode.OUTPUT)
    .build()

/**
 * A [GpioPort] which can read and write values of the [Peripheral] enumeration
 */
val decoderPort: Port<Peripheral> = deviceRegistry.create(portConfig)
    .mapped(Peripheral::of, Peripheral::pinMask)

/**
 * A [Pin] which, when low, outputs [Peripheral.MCP23S08.pinMask]
 */
val mcpEnablePin = decoderPort.pin(Peripheral.NONE, Peripheral.MCP23S08)
/**
 * A [Pin] which, when low, outputs [Peripheral.SHIFT_A.pinMask]
 */
val shiftAEnablePin = decoderPort.pin(Peripheral.NONE, Peripheral.SHIFT_A)
/**
 * A [Pin] which, when low, outputs [Peripheral.SHIFT_B.pinMask]
 */
val shiftBEnablePin = decoderPort.pin(Peripheral.NONE, Peripheral.SHIFT_B)
/**
 * A [Pin] which, when low, outputs [Peripheral.EEPROM.pinMask]
 */
val eepromEnablePin = decoderPort.pin(Peripheral.NONE, Peripheral.EEPROM)
```
```shell
INFO io.github.iamnicknack.pjs.mock.MockDeviceRegistry - Created MockGpioPort device with id: 3-to-8-enable-port
INFO io.github.iamnicknack.pjs.logging.LoggingDeviceRegistry - Created LoggingGpioPort device with id: 3-to-8-enable-port
```

In application code, it's now possible to have `Pin` devices that can be used to enable and disable up to eight peripherals using only four GPIOs:

```kotlin
assert(decoderPort.read() == Peripheral.NONE)               // read the current port value (should be `NONE`)

mcpEnablePin.low()                                          // enable the mcp23s08
assert(decoderPort.read() == Peripheral.MCP23S08)
shiftAEnablePin.low()                                       // enable the shift register
assert(decoderPort.read() == Peripheral.SHIFT_A)
shiftBEnablePin.low()                                       // enable the shift register
assert(decoderPort.read() == Peripheral.SHIFT_B)
eepromEnablePin.low()                                       // enable the eeprom
assert(decoderPort.read() == Peripheral.EEPROM)

decoderPort.write(Peripheral.NONE)                          // disable all the peripherals
assert(decoderPort.read() == Peripheral.NONE)
```
```shell
DEBUG device.MockGpioPort::3-to-8-enable-port - Reading port value: --------0----0--00--------------, 0
DEBUG device.MockGpioPort::3-to-8-enable-port - Writing port value: --------1----0--01--------------, 9
DEBUG device.MockGpioPort::3-to-8-enable-port - Reading port value: --------1----0--01--------------, 9
DEBUG device.MockGpioPort::3-to-8-enable-port - Writing port value: --------1----0--10--------------, 10
DEBUG device.MockGpioPort::3-to-8-enable-port - Reading port value: --------1----0--10--------------, 10
DEBUG device.MockGpioPort::3-to-8-enable-port - Writing port value: --------1----0--11--------------, 11
DEBUG device.MockGpioPort::3-to-8-enable-port - Reading port value: --------1----0--11--------------, 11
DEBUG device.MockGpioPort::3-to-8-enable-port - Writing port value: --------1----0--00--------------, 8
DEBUG device.MockGpioPort::3-to-8-enable-port - Reading port value: --------1----0--00--------------, 8
DEBUG device.MockGpioPort::3-to-8-enable-port - Writing port value: --------0----0--00--------------, 0
DEBUG device.MockGpioPort::3-to-8-enable-port - Reading port value: --------0----0--00--------------, 0
```

### Custom ports

Many enable pins could be used in conjunction with multiple SPI devices when there are more devices to control than hardware chip-selects available.

However, controlling chip-select for a device might not be considered "business logic". For example, it might not be desirable for application code to
have to explicitly enable and disable a device every time a value is written to it. E.g.:

```kotlin
var spi = deviceRegistry.create(SpiConfig.builder().id("test-spi").build())

mcpEnablePin.low()
spi.write(0x01)
mcpEnablePin.high()
```
```shell
INFO io.github.iamnicknack.pjs.mock.MockDeviceRegistry - Created MockSpi device with id: test-spi
INFO io.github.iamnicknack.pjs.logging.LoggingDeviceRegistry - Created LoggingSpi device with id: test-spi
DEBUG device.MockGpioPort::3-to-8-enable-port - Writing port value: --------1----0--01--------------, 9
DEBUG device.MockSpi::test-spi - Sending : 1 bytes: [01]
DEBUG device.MockSpi::test-spi - Received: 1 bytes: [00]
DEBUG device.MockGpioPort::3-to-8-enable-port - Writing port value: --------0----0--00--------------, 0
```

It's likely preferable for application code to be dependent only on a generic `Port<Int>`.

To abstract this type of requirement, it's possible to create a custom port implementation to do this:

```kotlin
/**
 * @param port a port which requires chip-enable
 * @param pin the enable-pin
 */
class EnablePort(
    private val port: Port<Int>,
    private val enablePin: Pin
) : Port<Int> {

    override fun read(): Int {
        enablePin.low()
        val result = port.read()
        enablePin.high()
        return result
    }

    override fun write(value: Int?) {
        enablePin.low()
        port.write(value)
        enablePin.high()
    }
}

/**
 * A usable port which does not require application code to explicitly control
 * the chip-enable signal
 */
val enabledPort: Port<Int> = EnablePort(spi, mcpEnablePin)
```

The mechanics of writing to the device are then taken care of, so application code can simply be dependent on a 
`Port<Int>`:

```kotlin
enabledPort.write(123)
```
```shell
DEBUG device.MockGpioPort::3-to-8-enable-port - Writing port value: --------1----0--01--------------, 9
DEBUG device.MockSpi::test-spi - Sending : 1 bytes: [7B]
DEBUG device.MockSpi::test-spi - Received: 1 bytes: [00]
DEBUG device.MockGpioPort::3-to-8-enable-port - Writing port value: --------0----0--00--------------, 0
```
