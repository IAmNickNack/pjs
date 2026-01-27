package io.github.iamnicknack.pjs.device.spi;

import io.github.iamnicknack.pjs.model.device.DeviceConfig;
import org.jspecify.annotations.Nullable;

/**
 * Configuration for a SPI device
 * @param baudRate SPI baud rate in Hz
 * @param bus the SPI hardware bus number
 * @param chipSelect the SPI chip-select pin number
 * @param mode the SPI mode (default 0)
 * @param bitsPerWord the SPI bits per word (default 8)
 * @param lsbFirst send data LSB first (default false)
 * @param id unique identifier for the device
 */
public record SpiConfig(
        int baudRate,
        int bus,
        int chipSelect,
        int mode,
        int bitsPerWord,
        boolean lsbFirst,
        String id
) implements DeviceConfig<Spi> {

    @Override
    public String getId() {
        return id;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int baudRate = 500000;
        private int bus = 0;
        private int chipSelect = 0;
        private int mode = 0;
        private int bitsPerWord = 8;
        private boolean lsbFirst = false;
        @Nullable
        private String id;

        public Builder baudRate(int baudRate) {
            this.baudRate = baudRate;
            return this;
        }

        public Builder bus(int bus) {
            this.bus = bus;
            return this;
        }

        public Builder chipSelect(int chipSelect) {
            this.chipSelect = chipSelect;
            return this;
        }

        public Builder mode(int mode) {
            this.mode = mode;
            return this;
        }

        public Builder bitsPerWord(int bitsPerWord) {
            this.bitsPerWord = bitsPerWord;
            return this;
        }

        public Builder lsbFirst(boolean lsbFirst) {
            this.lsbFirst = lsbFirst;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public SpiConfig build() {
            var id = (this.id != null) ? this.id : String.format("SPI-%d.%d", bus, chipSelect);
            return new SpiConfig(baudRate, bus, chipSelect, mode, bitsPerWord, lsbFirst, id);
        }
    }
}
