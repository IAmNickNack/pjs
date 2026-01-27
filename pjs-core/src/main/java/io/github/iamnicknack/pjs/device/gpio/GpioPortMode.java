package io.github.iamnicknack.pjs.device.gpio;

/**
 * The mode of a GPIO port, determining its behavior as input or output and any pull-up/pull-down resistors.
 */
public enum GpioPortMode {
    /**
     * The port is configured as an input without any internal pull-up or pull-down resistors.
     */
    INPUT(1),
    /**
     * The port is configured as an input with an internal pull-up resistor enabled.
     */
    INPUT_PULLUP(5),
    /**
     * The port is configured as an input with an internal pull-down resistor enabled.
     */
    INPUT_PULLDOWN(9),
    /**
     * The port is configured as a standard output.
     */
    OUTPUT(2),
    /**
     * The port is configured as an open-drain output.
     * In this mode, the output can either drive the line low or leave it floating (high impedance).
     */
    OUTPUT_OPENDRAIN(10),
    /**
     * The port is configured as an open-source output.
     * In this mode, the output can either drive the line high or leave it floating (
     */
    OUTPUT_OPENSOURCE(6);

    public final int value;

    GpioPortMode(int value) {
        this.value = value;
    }

    /**
     * Is the specified mode implied by this mode?
     * @param mode the mode to test
     * @return true if the bit mask matches
     */
    public boolean isSet(GpioPortMode mode) {
        return (value & mode.value) == mode.value;
    }
}
