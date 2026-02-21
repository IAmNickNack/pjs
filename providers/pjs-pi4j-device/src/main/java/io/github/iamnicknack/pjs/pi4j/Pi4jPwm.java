package io.github.iamnicknack.pjs.pi4j;

import com.pi4j.io.pwm.PwmPolarity;
import io.github.iamnicknack.pjs.device.pwm.Pwm;
import io.github.iamnicknack.pjs.device.pwm.PwmConfig;
import io.github.iamnicknack.pjs.model.device.DeviceConfig;

public class Pi4jPwm implements Pwm {

    private final PwmConfig config;
    private final com.pi4j.io.pwm.Pwm pwm;
    private final Runnable onClose;

    public Pi4jPwm(
            PwmConfig config,
            com.pi4j.io.pwm.Pwm pwm,
            Runnable onClose
    ) {
        this.config = config;
        this.pwm = pwm;
        this.onClose = onClose;
    }

    @Override
    public void setDutyRatio(double dutyRatio) {
        pwm.setDutyCycle((int)(dutyRatio * 100.0));
    }

    @Override
    public void setPeriod(long period) {
        throw new UnsupportedOperationException("Setting period is not supported by pi4j");
    }

    @Override
    public long getPeriod() {
        throw new UnsupportedOperationException("Getting period is not supported by pi4j");
    }

    @Override
    public void setDutyCycle(long dutyCycle) {
        throw new UnsupportedOperationException("Setting duty cycle is not supported by pi4j");
    }

    @Override
    public long getDutyCycle() {
        return pwm.getDutyCycle();
    }

    @Override
    public void setFrequency(int frequency) {
        pwm.setFrequency(frequency);
    }

    @Override
    public int getFrequency() {
        return pwm.getFrequency();
    }

    @Override
    public void setPolarity(Polarity polarity) {
        throw new UnsupportedOperationException("Setting polarity is not supported by pi4j");
    }

    @Override
    public Polarity getPolarity() {
        return (pwm.getPolarity() == PwmPolarity.INVERSED)
                ? Polarity.INVERTED
                : Polarity.NORMAL;
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
        return pwm.isOn();
    }

    @Override
    public void on() {
        pwm.on();
    }

    @Override
    public void off() {
        pwm.off();
    }

    @Override
    public DeviceConfig<Pwm> getConfig() {
        return config;
    }

    @Override
    public Boolean read() {
        return pwm.isOn();
    }

    @Override
    public void write(Boolean value) {
        if (value) {
            on();
        } else {
            off();
        }
    }

    @Override
    public void close() {
        off();
        onClose.run();
    }
}
