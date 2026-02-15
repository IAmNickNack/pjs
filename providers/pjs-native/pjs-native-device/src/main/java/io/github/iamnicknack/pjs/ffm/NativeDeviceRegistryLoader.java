package io.github.iamnicknack.pjs.ffm;

import io.github.iamnicknack.pjs.ffm.context.DefaultNativeContext;
import io.github.iamnicknack.pjs.ffm.context.NativeContext;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapperImpl;
import io.github.iamnicknack.pjs.ffm.device.NativeI2CProvider;
import io.github.iamnicknack.pjs.ffm.device.NativePortProvider;
import io.github.iamnicknack.pjs.ffm.device.NativePwmProvider;
import io.github.iamnicknack.pjs.ffm.device.NativeSpiProvider;
import io.github.iamnicknack.pjs.ffm.device.context.FileOperationsImpl;
import io.github.iamnicknack.pjs.ffm.device.context.GpioOperationsImpl;
import io.github.iamnicknack.pjs.ffm.device.context.IoctlOperationsImpl;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;
import io.github.iamnicknack.pjs.model.device.DeviceRegistryLoader;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

public class NativeDeviceRegistryLoader implements DeviceRegistryLoader {

    @Override
    public boolean isLoadable(Map<String, Object> properties) {
        return NativeContext.isAvailable() && Optional.ofNullable(properties.get("pjs.mode"))
                .filter(s -> s.equals("ffm"))
                .isPresent();
    }

    @Override
    public @Nullable DeviceRegistry load(Map<String, Object> properties) {
        var context = ServiceLoader.load(NativeContext.class, NativeContext.class.getClassLoader()).stream()
                .findFirst()
                .map(ServiceLoader.Provider::get)
                .orElseGet(DefaultNativeContext::new);

        var mapper = new MemorySegmentMapperImpl(context.getSegmentAllocator());

        var fileOperations = new FileOperationsImpl(context);
        var ioctlOperations = new IoctlOperationsImpl(context);
        var gpioOperations = new GpioOperationsImpl(fileOperations, ioctlOperations);
//        var pollingOperations = new PollingOperationsImpl(context);

        var i2cProvider = new NativeI2CProvider(fileOperations, ioctlOperations);
        var portProvider = new NativePortProvider(
                gpioOperations.chipInfo("/dev/gpiochip0"),
                context,
                fileOperations,
                ioctlOperations
        );
        var pwmProvider = new NativePwmProvider(fileOperations);
        var spiProvider = new NativeSpiProvider(
                fileOperations,
                ioctlOperations,
                mapper,
                context.getSegmentAllocator()
        );

        return new NativeDeviceRegistry(
                portProvider,
                spiProvider,
                pwmProvider,
                i2cProvider
        );
    }
}
