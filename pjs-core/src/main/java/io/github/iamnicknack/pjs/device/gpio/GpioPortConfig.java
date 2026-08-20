package io.github.iamnicknack.pjs.device.gpio;

import io.github.iamnicknack.pjs.model.device.DeviceConfig;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

/**
 * Configuration for a GPIO port which can be used by a {@link GpioPortFactory} to construct a {@link GpioPort} instance.
 * @param mask the pin mask
 * @param portMode initial portMode of the port
 * @param debounceDelay debounce delay in microseconds
 * @param id unique identifier for the port
 */
public record GpioPortConfig(
        int mask,
        GpioPortMode portMode,
        GpioEventMode eventMode,
        int defaultValue,
        int debounceDelay,
        String id
) implements DeviceConfig<GpioPort> {

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String getId() {
        return id;
    }

    public static class Builder {
        private int mask = 0;
        private GpioPortMode portMode = GpioPortMode.INPUT;
        private GpioEventMode eventMode = GpioEventMode.NONE;
        private int defaultValue = -1;
        private int debounceDelay = 0;
        @Nullable
        private String id;

        public Builder pin(int... pinNumber) {
            Arrays.stream(pinNumber).forEach(pin -> this.mask |= 1 << pin);
            return this;
        }

        public Builder pin(int pinNumber) {
            this.mask |= 1 << pinNumber;
            return this;
        }

        public Builder portMode(GpioPortMode mode) {
            this.portMode = mode;
            return this;
        }

        public Builder eventMode(GpioEventMode mode) {
            this.eventMode = mode;
            return this;
        }

        public Builder defaultValue(int value) {
            this.defaultValue = value;
            return this;
        }

        public Builder debounceDelay(int debounceDelay) {
            this.debounceDelay = debounceDelay;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public GpioPortConfig build() {
            var id = (this.id != null) ? this.id : String.format("GPIO-%s-%s", this.portMode, Integer.toBinaryString(this.mask));
            return new GpioPortConfig(mask, portMode, eventMode, defaultValue, debounceDelay, id);
        }
    }
}
