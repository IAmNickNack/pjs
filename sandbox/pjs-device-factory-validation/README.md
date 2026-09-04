# pjs-validation-device-factory

This module provides a factory decorator which can be configured with a known set of available hardware. 
It tracks and validates requests and provides detailed error messages when requests for hardware contain
invalid or unavailable selections.

Validation of hardware requests is based on data Raspberry Pi users can collect from the `pinctrl` utility. 
For example:

```
 0: a2    pn | hi // ID_SDA/GPIO0 = TXD1    <== TXD1 indicates UART TX
 1: a2    pu | hi // ID_SCL/GPIO1 = RXD1    <== RXD1 indicates UART RX
 2: no    pu | -- // GPIO2 = none           <== GPIO, not yet configured by the user
 3: no    pu | -- // GPIO3 = none
 4: no    pu | -- // GPIO4 = none
 5: no    pu | -- // GPIO5 = none
 6: no    pu | -- // GPIO6 = none
 7: op dh pu | hi // GPIO7 = output         <== GPIO, currently configured as output
 8: op dh pu | hi // GPIO8 = output
 9: a0    pn | lo // GPIO9 = SPI0_MISO      <== SPI0_MISO indicates SPI bus 0 COPI
10: a0    pn | lo // GPIO10 = SPI0_MOSI     <== SPI0_MOSI indicates SPI bus 0 CIPO
11: a0    pn | lo // GPIO11 = SPI0_SCLK     <== SPI0_SCLK indicates SPI bus 0 CLOCK
12: no    pd | -- // GPIO12 = none
13: no    pd | -- // GPIO13 = none
14: a3    pu | hi // GPIO14 = SDA3          <== SDA3 indicates I2C bus 3 SDA
15: a3    pu | hi // GPIO15 = SCL3          <== SCL3 indicates I2C bus 3 SCL
16: no    pd | -- // GPIO16 = none
17: no    pd | -- // GPIO17 = none
18: a3    pd | lo // GPIO18 = PWM0_CHAN2    <== PWM0_CHAN2 indicates PWM bus/chip 0 channel 2
19: no    pd | -- // GPIO19 = none
20: no    pd | -- // GPIO20 = none
21: no    pd | -- // GPIO21 = none
22: no    pd | -- // GPIO22 = none
23: no    pd | -- // GPIO23 = none
24: no    pd | -- // GPIO24 = none
25: no    pd | -- // GPIO25 = none
26: no    pd | -- // GPIO26 = none
27: no    pd | -- // GPIO27 = none
```

Validation uses attributes of this output to derive a lookup table of valid device configurations:  

| Hardware Allocation Mask           | Line Type | Bus | Channel | Name  | Offsets                                                                    |
|------------------------------------|-----------|-----|---------|-------|----------------------------------------------------------------------------|
| `-----11111111-11--11---1111111--` | GPIO      | -   | -       | GPIO  | 02, 03, 04, 05, 06, 07, 08, 12, 13, 16, 17, 19, 20, 21, 22, 23, 24, 25, 26 |
| `----------------11--------------` | I2C       | 3   | -       | I2C3  | 14, 15                                                                     |
| `-------------1------------------` | PWM       | 0   | 2       | PWM0  | 18                                                                         |
| `--------------------111---------` | SPI       | 0   | -       | SPI0  | 09, 10, 11                                                                 |
| `------------------------------11` | UART      | 1   | -       | UART1 | 00, 01                                                                     |

## Validating Available Hardware

To validate available hardware, the API derives an immutable index of the available data. Every call to
`GenericDeviceFactory::create` passes through a decorator of the function before any actual device construction
is attempted. For example:

User provides `GpioPortConfig`: The offsets are converted to a bit mask and and-ed 
with the GPIO hardware allocation mask. If the user requests offsets 9 and 10. These cannot be successfully and-ed with 
the hardware allocation mask and an exception can be reported.

User provides `I2CConfig` requesting bus 1: This does not exist in the lookup table and an exception can be reported.

This rather basic solution provides rudimentary validation of the user's requests and allows the API to provide 
useful error detail back to the user in cases of failure. For example, if the user requests GPIO on offsets 9 and 10,
an exception can be reported indicating that these offsets are not available, also providing an indictor of why 
the request failed. In this case, the offsets are already allocated to SPI bus 0.

## Tracking In-Use Hardware

The above approach can be adapted to also keep track of in-use hardware. I.e. hardware which has already been 
allocated to the user. 

In this case, we start with an empty, mutable collection and every successful call to `GenericDeviceFactory::create` 
updates the index. 

Querying the index is done in the opposite manner to the available hardware check. When checking for available hardware,
we check to confirm whether a request matches the immutable index. Conversely, when checking for in-use hardware,
we check to confirm whether a request does not match the mutable index.

### Returning an allocation to the pool

In an application which dynamically allocates and de-allocates hardware, the API needs to be notified when an allocation
can be returned to the pool so that the mutable index can be updated accordingly. 

This is handled automatically when the user code calls `Device::close()`.

## Enabling Hardware Validation

### LineSupplier

Hardware validation needs to know about the available hardware. Responsibility for providing this info
is delegated to a `LineSupplier` which provides a set of `Line` object which describes: the available pins, 
bus (where applicable) and channel (also, where applicable).

#### pinctrl

On a Raspberry Pi with `pinctrl` installed, this should be straight-forward. The `PinctrlLineSupplier` can 
fetch the required information by calling `pinctrl`: 

```kotlin
// kotlin

val lineSupplier = PinctrlLineSupplier()

lineSupplier.lines().forEach { logger.info("$it") }
```
Ought to produce the following output:
```
INFO  [main      ] [example-logger    ] Line(lineType=GPIO, name=GPIO, allocation=HardwareAllocation(mask=133902844), bus=null, channel=null)
INFO  [main      ] [example-logger    ] Line(lineType=I2C, name=I2C3, allocation=HardwareAllocation(mask=49152), bus=3, channel=null)
INFO  [main      ] [example-logger    ] Line(lineType=PWM, name=PWM0, allocation=HardwareAllocation(mask=262144), bus=0, channel=2)
INFO  [main      ] [example-logger    ] Line(lineType=SPI, name=SPI0, allocation=HardwareAllocation(mask=3584), bus=0, channel=null)
INFO  [main      ] [example-logger    ] Line(lineType=UART, name=UART1, allocation=HardwareAllocation(mask=3), bus=1, channel=null)
```

#### YAML

In other situations, it's possible to manually configure the same configuration in YAML format.

```yaml
# line-config.yaml

- lineType: "GPIO"
  name: "GPIO"
  allocation:
    - 2
    - 3
    - 4
    - 5
    - 6
    - 7
    - 8
- lineType: "SPI"
  name: "SPI0"
  bus: 0
  allocation:
    - 9
    - 10
    - 11
- lineType: "UART"
  name: "UART1"
  bus: 1
  allocation:
    - 0
    - 1
```
```kotlin
// kotlin

val lineSupplier = JacksonLineSupplier.from(Files.newBufferedReader("line-config.yaml"))

lineSupplier.lines().forEach { logger.info("$it") }
```
```
INFO  [main      ] [example-logger    ] Line(lineType=SPI, name=SPI0, allocation=HardwareAllocation(mask=3584), bus=0, channel=null)
INFO  [main      ] [example-logger    ] Line(lineType=UART, name=UART1, allocation=HardwareAllocation(mask=3), bus=1, channel=null)
INFO  [main      ] [example-logger    ] Line(lineType=GPIO, name=GPIO, allocation=HardwareAllocation(mask=508), bus=null, channel=null)
```

#### In code

The `LineSuppplier` can also be provided in code:

```kotlin
val lineSupplier: LineSupplier = {
    setOf(
        Line(LineType.GPIO, "GPIO", HardwareAllocation.fromOffsets(2, 4, 6)),
        Line(LineType.I2C, "I2C3", HardwareAllocation.fromOffsets(14, 15)),
    )
}

lineSupplier.lines().forEach { logger.info("$it") }
```
```
INFO  [main      ] [example-logger    ] Line(lineType=GPIO, name=GPIO, allocation=HardwareAllocation(mask=84), bus=null, channel=null)
INFO  [main      ] [example-logger    ] Line(lineType=I2C, name=I2C3, allocation=HardwareAllocation(mask=49152), bus=null, channel=null)
```
### Adding the Factory Decorator

Hardware validation is enabled as a factory decorator:

```kotlin
// kotlin

val factory = GenericDeviceFactory.builder()
    .factory(::MockDeviceFactory)
    .decorator { HardwareAllocationDeviceFactory(it, PinctrlLineSupplier()) }
    .decorator(::LoggingDeviceFactory)
    .build()
```

### Requesting Hardware

It's then possible to create a device as normal and have the `HardwareAllocationDeviceFactory` validate the hardware allocation:

```kotlin
// kotlin

logger.info("Creating port allocated to pin 2")
val port = factory.create(
    GpioPortConfig.builder()
        .id("port-pin-2")
        .pin(2)
        .build()
)
```
```
INFO  [main      ] [example-logger                              ] Creating port allocated to pin 2
DEBUG [main      ] [i.g.i.p.s.f.HardwareAllocationDeviceFactory ] Reserving line: Line(lineType=GPIO, name=port-pin-2, allocation=HardwareAllocation(mask=4), bus=null, channel=null)
DEBUG [main      ] [i.g.i.p.s.f.HardwareAllocationDeviceFactory ] > -----------------------------1-- - port-pin-2 : 02
```

Any attempt to create an invalid device will result in an error being logged and the creation failing:

```kotlin
// kotlin

logger.info("Attempting to create conflicting port")
try {
    factory.create(GpioPortConfig.builder().id("other-port").pin(2).build())
} catch (e: HardwareAllocationException.PinsInUse) {
    logger.error("Request failed for: {}", e.requested)
    logger.error("Hardware is already in use: " + e.conflicts)
}
```
```
INFO  [main      ] [example-logger    ] Attempting to create conflicting port
ERROR [main      ] [example-logger    ] Request failed for: Line(lineType=GPIO, name=other-port, allocation=HardwareAllocation(mask=4), bus=null, channel=null)
ERROR [main      ] [example-logger    ] Hardware is already in use: [Line(lineType=GPIO, name=port-pin-2, allocation=HardwareAllocation(mask=4), bus=null, c
```

### Releasing Hardware

Lines are release and returned to the pool when the device is closed:

```kotlin
// kotlin

logger.info("Closing port")
port.close()
```
```
INFO  [main      ] [example-logger                              ] Closing port
INFO  [main      ] [device.GpioPort.port-pin-2                  ] Closing GPIO port: port-pin-2
DEBUG [main      ] [i.g.i.p.s.f.HardwareAllocationDeviceFactory ] Releasing line: Line(lineType=GPIO, name=port-pin-2, allocation=HardwareAllocation(mask=4), bus=null, channel=null)
```

### Hardware Allocation Exceptions

| Exception              | Thrown when                                                                               |
|------------------------|-------------------------------------------------------------------------------------------|
| `PinsNotAvailable`     | The requested GPIO pins are unavailable in the current hardware configuration.            |
| `BusNotConfigured`     | The requested bus for a device type is unavailable in the current hardware configuration. |
| `ChannelNotConfigured` | THe requested PWM channel is unavailable in the current hardware configuration.           |
| `PinsInUse`            | A request includes GPIOs which have already been allocated.                               |
| `BusInUse`             | The requested I2C/SPI bus or PWM chip has already been allocated.                         |
