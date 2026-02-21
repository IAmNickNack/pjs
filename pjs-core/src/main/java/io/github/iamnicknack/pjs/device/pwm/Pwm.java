package io.github.iamnicknack.pjs.device.pwm;

import io.github.iamnicknack.pjs.model.device.Device;
import io.github.iamnicknack.pjs.model.pin.Pin;

/**
 * PWM contract for devices providing PWM functionality.
 */
public interface Pwm extends Pin, Device<Pwm> {
    /**
     * Set the duty cycle of the PWM.
     * @param dutyCycle The duty cycle in nanos.
     */
    void setDutyCycle(long dutyCycle);

    /**
     * Get the current duty cycle as a percentage.
     * @return The duty cycle as a percentage.
     */
    long getDutyCycle();

    /**
     * Set the duty cycle as a ratio of the period.
     * @param dutyRatio The duty cycle as a ratio of the period.
     */
    default void setDutyRatio(double dutyRatio) {
        if (dutyRatio > 1) {
            throw new IllegalArgumentException("dutyCycle must be <= 1");
        }

        if (dutyRatio < 0) {
            throw new IllegalArgumentException("dutyCycle must be >= 0");
        }

        setDutyCycle(dutyCycleFromRatio(dutyRatio, getPeriod()));
    }

    /**
     * Get the current duty cycle as a ratio of the period.
     * @return The duty cycle as a ratio of the period.
     */
    default double getDutyRatio() {
        return ratioFromDutyCycle(getDutyCycle(), getPeriod());
    }

    /**
     * Set the period of the PWM in nanos.
     * @param period The period in nanos.
     */
    void setPeriod(long period);

    /**
     * Get the current period in nanos.
     * @return The period in nanos.
     */
    long getPeriod();

    /**
     * Set the frequency of the PWM.
     * @param frequency The frequency in Hz.
     */
    default void setFrequency(int frequency) {
        if (frequency <= 0) {
            throw new IllegalArgumentException("frequency must be > 0");
        }
        setPeriod(periodFromFrequency(frequency));
    }

    /**
     * Get the current frequency in Hz.
     * @return The frequency in Hz.
     */
    default int getFrequency() {
        return frequencyFromPeriod(getPeriod());
    }

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

    static long periodFromFrequency(int frequency) {
        return 1_000_000_000 / frequency;
    }

    static int frequencyFromPeriod(long period) {
        return (period != 0)
                ? (int) (1_000_000_000 / period)
                : 0;
    }

    static long dutyCycleFromRatio(double ratio, long period) {
        return (long) (ratio * period);
    }

    static double ratioFromDutyCycle(long dutyCycle, long period) {
        return (period != 0)
                ? (double) dutyCycle / period
                : 0.0;
    }
}
