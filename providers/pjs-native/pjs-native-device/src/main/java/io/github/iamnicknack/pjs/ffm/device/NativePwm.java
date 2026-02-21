package io.github.iamnicknack.pjs.ffm.device;

import io.github.iamnicknack.pjs.device.pwm.Pwm;
import io.github.iamnicknack.pjs.device.pwm.PwmBean;
import io.github.iamnicknack.pjs.device.pwm.PwmConfig;
import io.github.iamnicknack.pjs.ffm.device.context.SysfsOperations;

import java.util.concurrent.TimeUnit;

class NativePwm extends PwmBean implements Pwm, AutoCloseable {

    private static final long NANOS_PER_SECOND = TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS);
    static final String ENABLE_PATH = "enable";
    static final String DUTY_CYCLE_PATH = "duty_cycle";
    static final String PERIOD_PATH = "period";
    static final String POLARITY_PATH = "polarity";

    private final SysfsOperations channelOperations;

    public NativePwm(PwmConfig config, SysfsOperations channelOperations) {
        super(config);
        this.channelOperations = channelOperations;
    }

    // Synchronized to avoid races with on/off
    @Override
    public synchronized void setDutyCycle(long dutyCycle) {
        super.setDutyCycle(dutyCycle);
        applySettings(); // write through to sysfs immediately
    }

    @Override
    public synchronized long getDutyCycle() {
        return super.getDutyCycle();
    }

    @Override
    public void setPeriod(long period) {
        super.setPeriod(period);
        applySettings();
    }

    @Override
    public long getPeriod() {
        return super.getPeriod();
    }

    @Override
    public synchronized void setFrequency(int frequency) {
        super.setFrequency(frequency);
        applySettings(); // write through to sysfs immediately
    }

    @Override
    public synchronized int getFrequency() {
        return super.getFrequency();
    }

    @Override
    public synchronized void setPolarity(Polarity polarity) {
        super.setPolarity(polarity);
        applySettings();
    }

    @Override
    public synchronized Polarity getPolarity() {
        return super.getPolarity();
    }

    @Override
    public synchronized void on() {
        // ensure hardware reflects current cached values using safe ordering
        applySettings();
        try {
            channelOperations.writeInt(ENABLE_PATH, 1);
            super.on();
        } catch (Exception ex) {
            System.err.println("Failed to enable PWM: " + ex);
            ex.printStackTrace(System.err);
            super.off();
        }
    }

    @Override
    public synchronized void off() {
        try {
            channelOperations.writeInt(ENABLE_PATH, 0);
        } catch (Exception ex) {
            System.err.println("Failed to disable PWM: " + ex);
            ex.printStackTrace(System.err);
        }
        super.off();
    }

    @Override
    public Boolean read() {
        return channelOperations.readInt(ENABLE_PATH) == 1;
    }

    @Override
    public synchronized void close() {
        off();
    }

    /**
     * Writes current frequency/dutyCycle to sysfs using a safe order:
     * - if enabled, disable first
     * - set duty to 0
     * - set period
     * - set duty to desired value
     * - if was enabled, re-enable
     */
    private void applySettings() {
        long nanos = NANOS_PER_SECOND / super.getFrequency();
        long dutyNanos = (nanos * super.getDutyCycle()) / 100;

        try {
            // If currently enabled, disable first to satisfy drivers that require it.
            boolean wasEnabled = super.read();
            if (wasEnabled) {
                channelOperations.writeInt(ENABLE_PATH, 0);
            }

            // Many drivers require duty <= period or require disabled writes.
            // Writing duty=0 first avoids transient invalid values.
            channelOperations.writeLong(DUTY_CYCLE_PATH, 0);
            channelOperations.writeLong(PERIOD_PATH, nanos);
            channelOperations.writeLong(DUTY_CYCLE_PATH, dutyNanos);
            channelOperations.writeString(POLARITY_PATH, getPolarity().value);

            if (wasEnabled) {
                // Re-enable if we were on
                channelOperations.writeInt(ENABLE_PATH, 1);
                super.on();
            }
        } catch (Exception ex) {
            // Minimal error handling; replace with a logger if available.
            System.err.println("Failed to apply PWM settings: " + ex);
            ex.printStackTrace(System.err);
        }
    }
}