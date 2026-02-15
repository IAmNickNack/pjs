package io.github.iamnicknack.pjs.device.gpio;

import io.github.iamnicknack.pjs.model.device.DeviceConfig;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * Configuration for a GPIO port which can be used by a {@link GpioPortProvider} to construct a {@link GpioPort} instance.
 * @param pinNumber array of pin numbers
 * @param portMode portMode of the port
 * @param debounceDelay debounce delay in microseconds
 * @param id unique identifier for the port
 */
public record GpioPortConfig(
        int[] pinNumber,
        GpioPortMode portMode,
        GpioEventMode eventMode,
        int defaultValue,
        int debounceDelay,
        String id
) implements DeviceConfig<GpioPort> {

    public static Builder builder() {
        return new Builder();
    }

    public int getPinMask() {
        return Arrays.stream(pinNumber).reduce(0, (acc, pin) -> acc | (1 << pin));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GpioPortConfig that = (GpioPortConfig) obj;
        return Arrays.equals(pinNumber, that.pinNumber)
                && portMode == that.portMode
                && eventMode == that.eventMode
                && defaultValue == that.defaultValue
                && debounceDelay == that.debounceDelay
                && Objects.equals(id, that.id);
    }

    @Override
    public String getId() {
        return id;
    }

    public static class Builder {
        private final ArrayList<Integer> pinNumber = new ArrayList<>();
        private GpioPortMode portMode = GpioPortMode.INPUT;
        private GpioEventMode eventMode = GpioEventMode.NONE;
        private int defaultValue = -1;
        private int debounceDelay = 0;
        @Nullable
        private String id;

        public Builder pin(int... pinNumber) {
            Arrays.stream(pinNumber).forEach(this.pinNumber::add);
            return this;
        }

        public Builder pin(int pinNumber) {
            this.pinNumber.add(pinNumber);
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
            int[] pinNumber = this.pinNumber.stream().mapToInt(Integer::intValue).toArray();
            var id = (this.id != null) ? this.id : String.format("GPIO-%s-%s", this.portMode, Arrays.toString(pinNumber));
            return new GpioPortConfig(pinNumber, portMode, eventMode, defaultValue, debounceDelay, id);
        }
    }
}
