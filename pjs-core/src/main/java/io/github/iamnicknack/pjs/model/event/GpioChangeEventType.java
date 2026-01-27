package io.github.iamnicknack.pjs.model.event;

/**
 * Line change event types
 */
public enum GpioChangeEventType {
    /**
     * No change
     */
    NONE(0),
    /**
     * The change was a rising edge.
     */
    RISING(1),
    /**
     * The change was a falling edge.
     */
    FALLING(2),
    /**
     * Any change.
     */
    ANY(3);

    public final int value;

    GpioChangeEventType(int value) {
        this.value = value;
    }

    static GpioChangeEventType from(int value) {
        return switch (value) {
            case 1 -> RISING;
            case 2 -> FALLING;
            case 3 -> ANY;
            default -> NONE;
        };
    }
}
