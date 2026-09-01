# Device Lifecycle

## Device Factories

The framework assumes that construction is a responsibility. This means that any logic required to create a device 
instance is encapsulated within a factory dedicated to that purpose. The factory is responsible for constructing or 
acquiring any dependencies required by a device instance. 

This leaves the device implementation free to focus on its core functionality without worrying about dependency 
management or instantiation details.

The utility of this approach varies depending on the complexity of the device and its dependencies. For example, 
a `MockGpioPort` is not particularly complex and does not directly benefit greatly from a dedicated factory. However, 
for more complex devices such as `GrpcGpioPort` which requires a gRPC client and managed resources, a factory can 
provide a clear separation of concerns, making the code more modular and easier to maintain.

Therefore, for consistency, all device implementations should follow this pattern, even if the factory is trivial.

```java
// java

@FunctionalInterface
public interface DeviceFactory<T extends Device<T>, V extends DeviceConfig<T>> {
    T create(V config);
}
```

A device factory can be any function that conforms to the `DeviceFactory` interface, returning a device instance when
provided with an appropriate configuration. This can be a method reference, a lambda expression, an anonymous class
or concrete implementation. 
(see Oracle documentation on [Method References](https://docs.oracle.com/javase/tutorial/java/javaOO/methodreferences.html))

Internally, the simplest factory might even be a method reference to the device constructor:

```java
DeviceFactory<GpioPort, GpioPortConfig> portFactory = MockGpioPortImpl::new;
```

However, because this might not always be the case, the framework exposes `DeviceFactory` to represent the concept 
of construction. This is an idiomatic PJs convention.

### Generic Device Factory

More common in the core modules is a `GenericDeviceFactory`:

```java
// java 

@FunctionalInterface
public interface GenericDeviceFactory {
    <T extends Device<T>, V extends DeviceConfig<T>> T create(V config);
}
```

This is essentially the same function as `DeviceFactory`, but with a generic type signature. It provides a single point
of entry for creating devices. An application can have a single factory via which all devices get created. 

It is the implementation's responsibility to ensure that the factory is capable of creating devices of the correct type.
`GenericDeviceFactory` implementations provided by the core modules all support the core device types of `GpioPort`, 
`I2C`, `Spi` and `Pwm`.

```java
// java

GenericDeviceFactory factory = new MockDeviceFactory();
// is functionally the same as:
DeviceFactory<GpioPort, GpioPortConfig> portFactory = factory::create;
// and:
DeviceFactory<I2C, I2CConfig> i2cFactory = factory::create;
// or:
DeviceFactory<Spi, SpiConfig> spiFactory = factory::create;
// or:
DeviceFactory<Pwm, PwmConfig> pwmFactory = factory::create;
```

### Generic Factory Builder

A `GenericDeviceFactory` can be constructed using a builder pattern, allowing the registration of specific device 
factories for different device types.

```java
// java 

var genericDeviceFactory = GenericDeviceFactory.builder()
        .factory(new MockGpioPortFactory(), GpioPortConfig.class)
        .factory(new MockI2CFactory(), I2CConfig.class)
        .factory(new MockSpiFactory(), SpiConfig.class)
        .factory(new MockPwmFactory(), PwmConfig.class)
        .build();
```

Because it is considered most likely that the user will generally want devices of the same implementations, implementation
providers in the core modules all provide a pre-configured `GenericDeviceFactory`.

The above code is functionally the same as:

```java
// java

var genericDeviceFactory = new MockDeviceFactory();
```

### Factory Decoration 

Additional functionality can be added to the factory during construction. 

For example, the `LoggingDeviceFactory` can be used to add logging to the devices created by a factory.

A decorator is a factory which delegates actual creation of devices to another factory and operates around the 
`create` method (similarly to AspectJ's `@Around` advice).

```kotlin
// kotlin

val factory: GenericDeviceFactory = GenericDeviceFactory.builder()
    .factory(::MockDeviceFactory)       // <== Root device factory
    .decorator(::LoggingDeviceFactory)  // <== Decorator
    .build()

val port: GpioPort = factory.create(GpioPortConfig.builder()
    .id("my-port")
    .pin(2, 3)
    .build()
)

port.write(3)
val value = port.read()
logger.info("Wrote and then read {} from the port", value)

port.close()
```

In this example, the device performing the actual operations is still a `MockGpioPort`. However, operations on the 
port instance are now logged as a result of the decoration:

```
DEBUG [main      ] [device.GpioPort.my-port   ] Writing port value: ----------------------------11--, 3
DEBUG [main      ] [device.GpioPort.my-port   ] Reading port value: ----------------------------11--, 3
INFO  [main      ] [example-logger            ] Wrote and then read 3 from the port
INFO  [main      ] [device.GpioPort.my-port   ] Closing GPIO port: port
```

The API does not restrict the number of decorators that can be applied to a factory, so the user is free to apply 
as many decorators as needed: 

```kotlin
// kotlin

/**
 * Hypothetical decorator that logs device creation
 */
val decoratorLogger: (GenericDeviceFactory) -> GenericDeviceFactory = { delegate ->
    object : GenericDeviceFactory {
        val logger: Logger = LoggerFactory.getLogger("decorator")
        override fun <T : Device<T>, V : DeviceConfig<T>> create(config: V): T {
            logger.info("Decorating device ${config.id} for ${config::class.simpleName}")
            return delegate.create(config)
        }
    }
}

val factory: GenericDeviceFactory = GenericDeviceFactory.builder()
    .factory(::MockDeviceFactory)
    .decorator(::LoggingDeviceFactory)
    .decorator(decoratorLogger)             // <== Additional decorator
    .build()


val port: GpioPort = factory.create(GpioPortConfig.builder().id("my-port").pin(2, 3).build())
val i2c: I2C = factory.create(I2CConfig.builder().id("my-i2c").bus(0).build())
// ... application activity
port.close()
i2c.close()
```

```
INFO  [main      ] [decorator                 ] Decorating device my-port for GpioPortConfig
INFO  [main      ] [decorator                 ] Decorating device my-i2c for I2CConfig
INFO  [main      ] [device.GpioPort.my-port   ] Closing GPIO port: my-port
INFO  [main      ] [device.I2C.my-i2c         ] Closing GPIO port: my-i2c
```

## Device Ownership

Devices created via a factory are owned by user code and implement `AutoCloseable`. When the device is 
no longer required, it should be disposed of by calling `close()`. 

Factories are not required to provide a release mechanism for the devices they create

## Device Registry

A `DeviceRegistry` is an extension of the `GenericDeviceFactory` which maintains a reference to the devices it creates.

The primary responsibility of the registry is to facilitate dynamic creation and later retrieval of devices at runtime.
To do this, it maintains a reference to each device it creates. To ensure these references can be disposed of, 
the registry is `AutoCloseable` and can be relied upon to close all dangling devices it creates when `close()` is called.

The user can still explicitly close a device to reclaim resource whenever necessary. However, they also have the 
option of deferring this as appropriate and close all devices at once by closing the registry.

