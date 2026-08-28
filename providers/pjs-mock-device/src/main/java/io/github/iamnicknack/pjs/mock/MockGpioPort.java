package io.github.iamnicknack.pjs.mock;

import io.github.iamnicknack.pjs.device.gpio.GpioPort;

public interface MockGpioPort extends GpioPort {
    /**
     * Update the value which backs the mock
     * @param value the new value
     */
    void mockValue(int value);

    /**
     * Fail the next call with an exception
     */
    void setFailWith(RuntimeException failWith);

    /**
     * @return the number of registered listeners
     */
    int getListenerCount();
}
