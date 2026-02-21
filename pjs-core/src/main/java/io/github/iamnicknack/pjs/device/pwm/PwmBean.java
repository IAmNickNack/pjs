package io.github.iamnicknack.pjs.device.pwm;

import io.github.iamnicknack.pjs.model.device.Device;
import io.github.iamnicknack.pjs.model.device.DeviceConfig;

/**
 * Common PWM properties
 */
public class PwmBean implements Device<Pwm>, Pwm {

    protected final PwmConfig config;

    private long dutyCycle;
    private long period;

    private Polarity polarity = Polarity.NORMAL;
    private boolean isOn;

    public PwmBean(PwmConfig config) {
        this.config = config;
        this.dutyCycle = config.dutyCycle();
        this.period = config.period();
        this.isOn = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeviceConfig<Pwm> getConfig() {
        return config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDutyCycle(long dutyCycle) {
        this.dutyCycle = dutyCycle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getDutyCycle() {
        return dutyCycle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPeriod(long period) {
        this.period = period;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getPeriod() {
        return this.period;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFrequency(int frequency) {
        if (frequency <= 0) {
            throw new IllegalArgumentException("frequency must be > 0");
        }
        this.period = 1_000_000_000 / frequency;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFrequency() {
        return this.period == 0 ? 0 : (int) (1_000_000_000 / period);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPolarity(Polarity polarity) {
        this.polarity = polarity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Polarity getPolarity() {
        return polarity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEnabled(boolean enabled) {
        if (enabled) {
            on();
        } else {
            off();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return isOn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void on() {
        this.isOn = true;
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void off() {
        this.isOn = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean read() {
        return isOn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(Boolean value) {
        this.isOn = value;
    }
}
