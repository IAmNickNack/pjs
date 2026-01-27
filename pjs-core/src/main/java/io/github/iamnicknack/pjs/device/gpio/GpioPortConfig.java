package io.github.iamnicknack.pjs.device.gpio;

import io.github.iamnicknack.pjs.model.device.DeviceConfig;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Configuration for a GPIO port which can be used by a {@link GpioPortProvider} to construct a {@link GpioPort} instance.
 * @param pinNumber array of pin numbers
 * @param mode mode of the port
 * @param id unique identifier for the port
 */
public record GpioPortConfig(
        int[] pinNumber,
        GpioPortMode mode,
        int defaultValue,
        String id
) implements DeviceConfig<GpioPort> {

    public static Builder builder() {
        return new Builder();
    }

    public int getPinMask() {
        return Arrays.stream(pinNumber).reduce(0, (acc, pin) -> acc | (1 << pin));
    }

    @Override
    public String getId() {
        return id;
    }

    public static class Builder {
        private final ArrayList<Integer> pinNumber = new ArrayList<>();
        private GpioPortMode mode = GpioPortMode.INPUT;
        private int defaultValue = -1;
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

        public Builder mode(GpioPortMode mode) {
            this.mode = mode;
            return this;
        }

        public Builder defaultValue(int value) {
            this.defaultValue = value;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public GpioPortConfig build() {
            int[] pinNumber = this.pinNumber.stream().mapToInt(Integer::intValue).toArray();
            var id = (this.id != null) ? this.id : String.format("GPIO-%s-%s", this.mode, Arrays.toString(pinNumber));
            return new GpioPortConfig(pinNumber, mode, defaultValue, id);
        }
    }
}
