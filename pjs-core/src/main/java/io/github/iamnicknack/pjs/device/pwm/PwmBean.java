package io.github.iamnicknack.pjs.device.pwm;

import io.github.iamnicknack.pjs.model.device.Device;
import io.github.iamnicknack.pjs.model.device.DeviceConfig;

/**
 * Common PWM properties
 */
public class PwmBean implements Device<Pwm>, Pwm {

    protected final PwmConfig config;

    private int dutyCycle;
    private int frequency;
    private Polarity polarity = Polarity.NORMAL;
    private boolean isOn;

    public PwmBean(PwmConfig config) {
        this.config = config;
        this.dutyCycle = config.dutyCycle();
        this.frequency = config.frequency();
        this.isOn = false;
    }

    @Override
    public DeviceConfig<Pwm> getConfig() {
        return config;
    }

    @Override
    public void setDutyCycle(int dutyCycle) {
        if (dutyCycle < 0 || dutyCycle > 100) {
            throw new IllegalArgumentException("dutyCycle must be 0..100");
        }
        this.dutyCycle = dutyCycle;
    }

    @Override
    public int getDutyCycle() {
        return dutyCycle;
    }

    @Override
    public void setFrequency(int frequency) {
        if (frequency <= 0) {
            throw new IllegalArgumentException("frequency must be > 0");
        }
        this.frequency = frequency;
    }

    @Override
    public int getFrequency() {
        return frequency;
    }

    @Override
    public void setPolarity(Polarity polarity) {
        this.polarity = polarity;
    }

    @Override
    public Polarity getPolarity() {
        return polarity;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled) {
            on();
        } else {
            off();
        }
    }

    @Override
    public boolean isEnabled() {
        return isOn;
    }

    @Override
    public void on() {
        this.isOn = true;
    }

    @Override
    public void off() {
        this.isOn = false;
    }

    @Override
    public Boolean read() {
        return isOn;
    }

    @Override
    public void write(Boolean value) {
        this.isOn = value;
    }
}
