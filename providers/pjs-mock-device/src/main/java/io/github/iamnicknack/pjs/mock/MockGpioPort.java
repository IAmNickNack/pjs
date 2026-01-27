package io.github.iamnicknack.pjs.mock;

import io.github.iamnicknack.pjs.device.gpio.GpioPort;
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.device.gpio.GpioPortMode;
import io.github.iamnicknack.pjs.model.event.GpioChangeEvent;
import io.github.iamnicknack.pjs.model.event.GpioChangeEventType;
import io.github.iamnicknack.pjs.model.event.GpioEventEmitterDelegate;
import io.github.iamnicknack.pjs.model.event.GpioEventListener;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockGpioPort implements GpioPort {

    private final Logger logger = LoggerFactory.getLogger(MockGpioPort.class);

    private int portValue = 0;
    private final GpioPortConfig config;
    private final GpioEventEmitterDelegate<GpioPort> eventEmitterDelegate = new GpioEventEmitterDelegate<>();

    @Nullable
    private RuntimeException failWith = null;

    public MockGpioPort(GpioPortConfig config) {
        this.config = config;
        if (config.defaultValue() >= 0) {
            portValue = config.defaultValue();
        }
    }

    @Override
    public GpioPortConfig getConfig() {
        return config;
    }

    @Override
    public Integer read() {
        maybeThrow();
        return portValue;
    }

    @Override
    public void write(Integer value) {
        maybeThrow();
        portValue = value;
    }

    public int getListenerCount() {
        return eventEmitterDelegate.getListenerCount();
    }

    @Override
    public void addListener(GpioEventListener<GpioPort> listener) {
        logger.info("Adding mock GPIO port listener: {}", listener);
        maybeThrow();
        eventEmitterDelegate.addListener(listener);
    }

    @Override
    public void removeListener(GpioEventListener<GpioPort> listener) {
        logger.info("Removing mock GPIO port listener: {}", listener);
        maybeThrow();
        eventEmitterDelegate.removeListener(listener);
    }

    /**
     * Update the value which backs the mock
     * @param value the new value
     */
    public void mockValue(int value) {
        if (config.mode().isSet(GpioPortMode.INPUT)) {
            var eventType = eventTypeForValue(value);
            if (eventType != GpioChangeEventType.NONE) {
                logger.debug("Mocking value change on port {}: {} -> {}", config.id(), portValue, value);
                portValue = value;
                eventEmitterDelegate.onEvent(new GpioChangeEvent<>(this, eventType));
            }
        } else {
            throw new IllegalStateException("Cannot set values on mock output. Use `write` operations.");
        }
    }

    public void setFailWith(RuntimeException failWith) {
        this.failWith = failWith;
    }

    private void maybeThrow() {
        if (failWith != null) {
            var err = this.failWith;
            this.failWith = null;
            throw err;
        }
    }

    private GpioChangeEventType eventTypeForValue(int newValue) {
        int diff = newValue - portValue;
        if (diff == 0) {
            return GpioChangeEventType.NONE;
        } else if (diff > 0) {
            return GpioChangeEventType.RISING;
        } else {
            return GpioChangeEventType.FALLING;
        }
    }
}
