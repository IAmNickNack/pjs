package io.github.iamnicknack.pjs.device.pwm;

import io.github.iamnicknack.pjs.model.device.DeviceConfig;
import org.jspecify.annotations.Nullable;

/**
 * Configuration for a PWM channel
 * @param chip the PWM chip number
 * @param channel the PWM channel number
 * @param frequency the initial frequency in Hz
 * @param dutyCycle the initial duty cycle as a percentage
 */
public record PwmConfig(
        int chip,
        int channel,
        int frequency,
        int dutyCycle,
        Pwm.Polarity polarity,
        String getId
) implements DeviceConfig<Pwm> {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int chip;
        private int channel;
        private int frequency = 440; // Default frequency 440Hz (A4)
        private int dutyCycle = 0; // Default duty cycle 0%
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

        public Builder frequency(int frequency) {
            this.frequency = frequency;
            return this;
        }

        public Builder dutyCycle(int dutyCycle) {
            this.dutyCycle = dutyCycle;
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
            return new PwmConfig(chip, channel, frequency, dutyCycle, polarity, id);
        }
    }
}
