package io.github.iamnicknack.pjs.device.gpio;

/**
 * GPIO event portMode.
 */
public enum GpioEventMode {
    /**
     * No events will be captured.
     */
    NONE(0),
    /**
     * Capture events on the rising edge.
     */
    RISING(1),
    /**
     * Capture events on the falling edge.
     */
    FALLING(2),
    /**
     * Capture events on both edges.
     */
    BOTH(3);


    public final int value;

    GpioEventMode(int value) {
        this.value = value;
    }


}
