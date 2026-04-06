package io.github.iamnicknack.pjs.model.pin;

import io.github.iamnicknack.pjs.model.port.Port;

/**
 * A pin controls binary, on/off state.
 * <p>
 * For example, GPIO and PWM can both be expressed as pins.
 * </p>
 */
public interface Pin extends Port<Boolean> {

    /**
     * The default behaviour is to write true for high.
     */
    default void high() {
        this.write(true);
    }

    /**
     * The default behaviour is to write false for low.
     */
    default void low() {
        this.write(false);
    }

    /**
     * Returns a pin that does nothing when written to or read from.
     * @return a noop pin
     */
    static Pin noop() {
        return noop(false);
    }

    /**
     * Returns a pin that does nothing when written to or read from.
     * @param value the value to return when read
     * @return a noop pin
     */
    static Pin noop(boolean value) {
        return new Pin() {
            @Override
            public Boolean read() {
                return value;
            }

            @Override
            public void write(Boolean ignored) {
                // no nothing
            }
        };
    }
}
