package io.github.iamnicknack.pjs.device.pwm;

import io.github.iamnicknack.pjs.model.device.Device;
import io.github.iamnicknack.pjs.model.pin.Pin;

/**
 * PWM contract for devices providing PWM functionality.
 */
public interface Pwm extends Pin, Device<Pwm> {
    /**
     * Set the duty cycle of the PWM.
     * @param dutyCycle The duty cycle as a percentage.
     */
    void setDutyCycle(int dutyCycle);

    /**
     * Get the current duty cycle as a percentage.
     */
    int getDutyCycle();

    /**
     * Set the frequency of the PWM.
     * @param frequency The frequency in Hz.
     */
    void setFrequency(int frequency);

    /**
     * Get the current frequency in Hz.
     */
    int getFrequency();

    /**
     * Set the signal polarity.
     * @param polarity The polarity.
     */
    void setPolarity(Polarity polarity);

    /**
     * Get the current signal polarity.
     */
    Polarity getPolarity();

    /**
     * Enable or disable the PWM.
     * @param enabled true to enable, false to disable
     */
    void setEnabled(boolean enabled);

    /**
     * Check if the PWM is enabled.
     * @return true if enabled, false otherwise
     */
    boolean isEnabled();

    /**
     * Turn PWM on
     */
    void on();

    /**
     * Turn PWM off
     */
    void off();

    /**
     * Alias for {@link #on()}
     */
    default void high() {
        on();
    }

    /**
     * Alias for {@link #off()}
     */
    default void low() {
        off();
    }

    /**
     * Polarity of the PWM signal.
     */
    enum Polarity {
        NORMAL("normal"),
        INVERTED("inversed");

        public final String value;

        Polarity(String value) {
            this.value = value;
        }
    }
}
