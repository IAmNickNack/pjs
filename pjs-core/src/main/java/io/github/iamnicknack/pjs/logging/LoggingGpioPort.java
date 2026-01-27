package io.github.iamnicknack.pjs.logging;

import io.github.iamnicknack.pjs.device.gpio.GpioPort;
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.model.device.DeviceConfig;
import io.github.iamnicknack.pjs.model.device.WithDelegateDevice;
import io.github.iamnicknack.pjs.model.event.GpioEventListener;
import io.github.iamnicknack.pjs.util.GpioPinMask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link GpioPort} decorator that logs all I/O operations performed on the underlying port.
 */
public class LoggingGpioPort implements GpioPort, WithDelegateDevice<GpioPort> {

    private final GpioPort delegate;
    private final Logger logger;
    private final GpioPinMask pinsMask;

    public LoggingGpioPort(GpioPort delegate) {
        this.delegate = delegate;
        this.pinsMask = new GpioPinMask(((GpioPortConfig)delegate.getConfig()).pinNumber());
        this.logger = LoggerFactory.getLogger("device." + delegate.getClass().getSimpleName() + "::" + delegate.getConfig().getId());
    }

    @Override
    public DeviceConfig<GpioPort> getConfig() {
        return delegate.getConfig();
    }

    @Override
    public void addListener(GpioEventListener<GpioPort> listener) {
        logger.debug("Adding GPIO port listener: {}", listener);
        delegate.addListener(listener);
    }

    @Override
    public void removeListener(GpioEventListener<GpioPort> listener) {
        logger.debug("Removing GPIO port listener: {}", listener);
        delegate.removeListener(listener);
    }

    @Override
    public Integer read() {
        int value = delegate.read();
        logger.debug("Reading port value: {}, {}", pinsMask.getMaskString(value), value);
        return value;
    }

    @Override
    public void write(Integer value) {
        delegate.write(value);
        logger.debug("Writing port value: {}, {}", pinsMask.getMaskString(value), value);
    }

    @Override
    public void close() throws Exception {
        delegate.close();
    }

    @Override
    public GpioPort getDelegate() {
        return delegate;
    }
}
