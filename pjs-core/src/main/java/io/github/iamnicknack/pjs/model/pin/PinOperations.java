package io.github.iamnicknack.pjs.model.pin;

/**
 * Extended pin operations
 */
public interface PinOperations extends Pin {
    /**
     * Pulse the pin. If the pin is low, it will go high and return to low.
     * If the pin is high, it will go low and return to high.
     */
    void pulse();

    /**
     * Toggle the pin state. If the pin is low, it will go high and vice versa.
     */
    void toggle();
}
