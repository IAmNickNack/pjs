package io.github.iamnicknack.pjs.impl;

import io.github.iamnicknack.pjs.device.gpio.GpioPort;
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.mock.MockDevice;
import io.github.iamnicknack.pjs.mock.MockDeviceFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrackingDeviceFactoryTest {

    @Test
    @SuppressWarnings("unchecked")
    void deviceIsClosedWhenRegistryIsClosed() {
        var factory = new TrackingDeviceFactory(new MockDeviceFactory());

        var device = factory.create(GpioPortConfig.builder().id("test-port").pin(1).build());
        var proxy = (TrackingDeviceFactory.TrackingProxy) device;
        var mockDevice = (MockDevice<GpioPort>) proxy.getDelegate();

        assertFalse(mockDevice.isClosed(), "Device should not initially be closed");

        factory.close();

        assertTrue(mockDevice.isClosed(), "Device should be closed after factory is closed");
    }

    @Test
    @SuppressWarnings("unchecked")
    void registryIsUpdatedWhenDeviceIsClosed() throws Exception {
        var factory = new TrackingDeviceFactory(new MockDeviceFactory());

        var device = factory.create(GpioPortConfig.builder().id("test-port").pin(1).build());
        var proxy = (TrackingDeviceFactory.TrackingProxy) device;
        var mockDevice = (MockDevice<GpioPort>) proxy.getDelegate();

        assertFalse(mockDevice.isClosed(), "Device should not initially be closed");
        assertTrue(factory.contains("test-port"), "Factory should contain the device");

        device.close();

        assertFalse(factory.contains("test-port"), "Factory should not contain the device after closing");
        assertTrue(mockDevice.isClosed(), "Device should be closed");

        factory.close();
    }
}