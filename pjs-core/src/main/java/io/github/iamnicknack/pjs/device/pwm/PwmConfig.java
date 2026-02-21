package io.github.iamnicknack.pjs.device.pwm;

import io.github.iamnicknack.pjs.model.device.DeviceConfig;
import org.jspecify.annotations.Nullable;

/**
 * Configuration for a PWM channel
 * @param chip the PWM chip number
 * @param channel the PWM channel number
 * @param period the initial pwm period in nanoseconds
 * @param dutyCycle the initial duty cycle in nanoseconds
 */
public record PwmConfig(
        int chip,
        int channel,
        long period,
        long dutyCycle,
        Pwm.Polarity polarity,
        String getId
) implements DeviceConfig<Pwm> {

    public int frequency() {
        return (int) (1_000_000_000 / period);
    }

    public int dutyCyclePercent() {
        return (int) (dutyCycle * 100 / period);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int chip;
        private int channel;
        private long period = 0;
        private long dutyCycle = 0; // Default duty cycle nanos
        private Pwm.Polarity polarity = Pwm.Polarity.NORMAL;
        @Nullable
        private String id = null;

        public Builder chip(int chip) {
            this.chip = chip;
            return this;
        }

        public Builder channel(int channel) {
            this.channel = channel;
            return this;
        }

        public Builder period(long period) {
            this.period = period;
            return this;
        }

        public Builder dutyCycle(long dutyCycle) {
            this.dutyCycle = dutyCycle;
            return this;
        }

        public Builder frequency(long frequency) {
            this.period = 1_000_000_000 / frequency;
            return this;
        }

        public Builder polarity(Pwm.Polarity polarity) {
            this.polarity = polarity;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public PwmConfig build() {
            var id = (this.id != null) ? this.id : String.format("PWM-%d-%d", chip, channel);
            return new PwmConfig(chip, channel, period, dutyCycle, polarity, id);
        }
    }
}
