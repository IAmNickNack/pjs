package io.github.iamnicknack.pjs.device.i2c;

import io.github.iamnicknack.pjs.model.device.DeviceConfig;
import org.jspecify.annotations.Nullable;

/**
 * Configuration for an I2C bus
 * @param bus the I2C hardware bus number
 * @param getId unique identifier for the device
 */
public record I2CConfig(
        int bus,
        String getId
) implements DeviceConfig<I2C> {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int bus;
        @Nullable
        private String id;

        public Builder bus(int bus) {
            this.bus = bus;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public I2CConfig build() {
            var id = (this.id != null) ? this.id : String.format("I2CBus-%d", bus);
            return new I2CConfig(bus, id);
        }
    }
}
