package io.github.iamnicknack.pjs.ffm;

import io.github.iamnicknack.pjs.ffm.context.DefaultNativeContext;
import io.github.iamnicknack.pjs.ffm.context.NativeContext;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapperImpl;
import io.github.iamnicknack.pjs.ffm.device.NativeI2CFactory;
import io.github.iamnicknack.pjs.ffm.device.NativePortFactory;
import io.github.iamnicknack.pjs.ffm.device.NativePwmFactory;
import io.github.iamnicknack.pjs.ffm.device.NativeSpiFactory;
import io.github.iamnicknack.pjs.ffm.device.context.FileOperationsImpl;
import io.github.iamnicknack.pjs.ffm.device.context.GpioOperationsImpl;
import io.github.iamnicknack.pjs.ffm.device.context.IoctlOperationsImpl;
import io.github.iamnicknack.pjs.ffm.device.context.PollingOperationsImpl;
import io.github.iamnicknack.pjs.ffm.event.EventPollerFactoryImpl;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;
import io.github.iamnicknack.pjs.model.device.DeviceRegistryLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

public class NativeDeviceRegistryLoader implements DeviceRegistryLoader<DeviceRegistryLoader.NoConfig> {

    private final Logger logger = LoggerFactory.getLogger(NativeDeviceRegistryLoader.class);

    @Override
    public boolean isLoadable(Map<String, Object> properties) {
        return NativeContext.isAvailable() && Optional.ofNullable(properties.get("pjs.mode"))
                .filter("ffm"::equals)
                .isPresent();
    }

    @Override
    public DeviceRegistry load() {
        return load(DeviceRegistryLoader.NoConfig.INSTANCE);
    }

    @Override
    public DeviceRegistry load(Map<String, Object> ignored) {
        return load(DeviceRegistryLoader.NoConfig.INSTANCE);
    }

    @Override
    public DeviceRegistry load(NoConfig ignored) {
        var context = ServiceLoader.load(NativeContext.class, NativeContext.class.getClassLoader()).stream()
                .findFirst()
                .map(ServiceLoader.Provider::get)
                .orElseGet(DefaultNativeContext::new);

        var mapper = new MemorySegmentMapperImpl(context.getSegmentAllocator());

        var fileOperations = new FileOperationsImpl(context);
        var ioctlOperations = new IoctlOperationsImpl(context);
        var gpioOperations = new GpioOperationsImpl(fileOperations, ioctlOperations);
        var pollingOperations = new PollingOperationsImpl(context);
        var eventPollerFactory = new EventPollerFactoryImpl(
                Duration.ofMillis(100),
                pollingOperations,
                fileOperations,
                4
        );

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down native device registry");
            eventPollerFactory.close();
        }));

        var i2cFactory = new NativeI2CFactory(fileOperations, ioctlOperations);
        var portFactory = new NativePortFactory(
                gpioOperations.chipInfo("/dev/gpiochip0"),
                fileOperations,
                ioctlOperations,
                eventPollerFactory
        );
        var pwmFactory = new NativePwmFactory(fileOperations);
        var spiFactory = new NativeSpiFactory(
                fileOperations,
                ioctlOperations,
                mapper,
                context.getSegmentAllocator()
        );

        return new NativeDeviceFactory(
                portFactory,
                spiFactory,
                pwmFactory,
                i2cFactory
        ).asDeviceRegistry();
    }
}
