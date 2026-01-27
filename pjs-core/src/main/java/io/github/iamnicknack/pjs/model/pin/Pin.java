package io.github.iamnicknack.pjs.model.pin;

import io.github.iamnicknack.pjs.model.port.Port;

/**
 * A pin controls binary, on/off state.
 * <p>
 * For example, GPIO and PWM can both be expressed as pins.
 * </p>
 */
public interface Pin extends Port<Boolean> {
    default void high() {
        this.write(true);
    }

    default void low() {
        this.write(false);
    }
}
