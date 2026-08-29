package io.github.iamnicknack.pjs.ffm;

import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.device.i2c.I2CConfig;
import io.github.iamnicknack.pjs.device.pwm.PwmConfig;
import io.github.iamnicknack.pjs.device.spi.SpiConfig;
import io.github.iamnicknack.pjs.ffm.device.NativeI2CFactory;
import io.github.iamnicknack.pjs.ffm.device.NativePortFactory;
import io.github.iamnicknack.pjs.ffm.device.NativePwmFactory;
import io.github.iamnicknack.pjs.ffm.device.NativeSpiFactory;
import io.github.iamnicknack.pjs.model.device.Device;
import io.github.iamnicknack.pjs.model.device.DeviceConfig;
import io.github.iamnicknack.pjs.model.device.GenericDeviceFactory;

public class NativeDeviceFactory implements GenericDeviceFactory {

    private final GenericDeviceFactory factory;

    public NativeDeviceFactory(
            NativePortFactory nativePortFactory,
            NativeSpiFactory nativeSpiFactory,
            NativePwmFactory nativePwmFactory,
            NativeI2CFactory nativeI2CFactory
    ) {
        this.factory = GenericDeviceFactory.builder()
                .factory(nativePortFactory, GpioPortConfig.class)
                .factory(nativeSpiFactory, SpiConfig.class)
                .factory(nativePwmFactory, PwmConfig.class)
                .factory(nativeI2CFactory, I2CConfig.class)
                .build();
    }

    @Override
    public <T extends Device<T>, V extends DeviceConfig<T>> T create(V config) {
        return factory.create(config);
    }
}
