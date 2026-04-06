package io.github.iamnicknack.pjs.model.pin;

import java.util.concurrent.Callable;

/**
 * A pin that can be used to enable or disable a device.
 */
public interface EnablePin extends Pin {

    /**
     * Enable the device.
     */
    void enable();

    /**
     * Disable the device.
     */
    void disable();

    /**
     * Check if the device is enabled.
     * @return true if enabled, false otherwise
     */
    default boolean isEnabled() {
        return read();
    }

    /**
     * Pulse the enable line.
     */
    default void pulse() {
        enable();
        disable();
    }

    /**
     * Enable the device around a runnable.
     * @param runnable the runnable to run
     */
    default void enableAround(Runnable runnable) {
        enableAround(() -> {
            runnable.run();
            return null;
        });
    }

    /**
     * Enable the device around a callable and return the result.
     * @param callable the callable to run
     * @return the result of the callable
     * @param <T> the type of the callable result
     */
    default <T> T enableAround(Callable<T> callable) {
        try {
            enable();
            return callable.call();
        } catch (Exception e) {
            disable();
            throw new RuntimeException("Error while enabling pin", e);
        } finally {
            disable();
        }
    }

    /**
     * Pulse the enable line after a runnable has been run.
     * @param runnable the runnable to run
     */
    default void pulseAfter(Runnable runnable) {
        pulseAfter(() -> {
            runnable.run();
            return null;
        });
    }

    /**
     * Pulse the enable line after a callable has been run.
     * @param callable the callable to run
     * @return the result of the callable
     * @param <T> the type of the callable result
     */
    default <T> T pulseAfter(Callable<T> callable) {
        try {
            var result = callable.call();
            enable();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            disable();
        }
    }

    /**
     * Wrap this enable pin around another one.
     * @param pin the pin to wrap
     * @return the wrapped enable pin
     */
    default EnablePin around(EnablePin pin) {
        return new EnablePin() {
            @Override
            public void enable() {
                EnablePin.this.enable();
                pin.enable();
            }

            @Override
            public void disable() {
                pin.disable();
                EnablePin.this.disable();
            }

            @Override
            public boolean isEnabled() {
                return pin.isEnabled();
            }

            @Override
            public Boolean read() {
                return pin.read();
            }

            @Override
            public void write(Boolean value) {
                pin.write(value);
            }
        };
    }

    /**
     * Create an active high enable pin.
     * @param pin the pin to wrap
     * @return the enable pin
     */
    static EnablePin activeHigh(Pin pin) {
        return new EnablePin() {
            @Override
            public void enable() {
                pin.high();
            }

            @Override
            public void disable() {
                pin.low();
            }

            @Override
            public boolean isEnabled() {
                return pin.read();
            }

            @Override
            public Boolean read() {
                return pin.read();
            }

            @Override
            public void write(Boolean value) {
                pin.write(value);
            }
        };
    }

    /**
     * Create an active low enable pin.
     * @param pin the pin to wrap
     * @return the enable pin
     */
    static EnablePin activeLow(Pin pin) {
        return new EnablePin() {
            @Override
            public void enable() {
                pin.low();
            }

            @Override
            public void disable() {
                pin.high();
            }

            @Override
            public boolean isEnabled() {
                return !pin.read();
            }

            @Override
            public Boolean read() {
                return pin.read();
            }

            @Override
            public void write(Boolean value) {
                pin.write(value);
            }
        };
    }

    /**
     * Create a noop enable pin.
     * @return the noop enable pin
     */
    static EnablePin noop() {
        return noop(false);
    }

    /**
     * Create a noop enable pin.
     * @param enabled the initial state of the pin
     * @return the noop enable pin
     */
    static EnablePin noop(boolean enabled) {
        return activeHigh(Pin.noop(enabled));
    }
}
