package io.github.iamnicknack.pjs.logging;

import io.github.iamnicknack.pjs.device.pwm.Pwm;
import io.github.iamnicknack.pjs.model.device.DeviceConfig;
import io.github.iamnicknack.pjs.model.device.WithDelegateDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingPwm implements Pwm, WithDelegateDevice<Pwm> {

    private final Pwm delegate;
    private final Logger logger;

    public LoggingPwm(Pwm delegate) {
        this.delegate = delegate;
        this.logger = LoggerFactory.getLogger("device." + delegate.getClass().getSimpleName() + "::" + delegate.getConfig().getId());
    }

    @Override
    public void setDutyCycle(int dutyCycle) {
        logger.debug("Setting duty cycle: {}", dutyCycle);
        delegate.setDutyCycle(dutyCycle);
    }

    @Override
    public int getDutyCycle() {
        return delegate.getDutyCycle();
    }

    @Override
    public void setFrequency(int frequency) {
        logger.debug("Setting frequency: {}", frequency);
        delegate.setFrequency(frequency);
    }

    @Override
    public int getFrequency() {
        return delegate.getFrequency();
    }

    @Override
    public void setPolarity(Polarity polarity) {
        logger.debug("Setting polarity: {}", polarity);
        delegate.setPolarity(polarity);
    }

    @Override
    public Polarity getPolarity() {
        return delegate.getPolarity();
    }

    @Override
    public void setEnabled(boolean enabled) {
        delegate.setEnabled(enabled);
    }

    @Override
    public boolean isEnabled() {
        return delegate.isEnabled();
    }

    @Override
    public void on() {
        logger.debug("on");
        delegate.on();
    }

    @Override
    public void off() {
        logger.debug("off");
        delegate.off();
    }

    @Override
    public DeviceConfig<Pwm> getConfig() {
        return delegate.getConfig();
    }

    @Override
    public Pwm getDelegate() {
        return delegate;
    }

    @Override
    public void close() throws Exception {
        delegate.close();
    }

    @Override
    public Boolean read() {
        return delegate.read();
    }

    @Override
    public void write(Boolean value) {
        delegate.write(value);
    }
}
