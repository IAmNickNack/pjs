package io.github.iamnicknack.pjs.pi4j;

import io.github.iamnicknack.pjs.device.spi.Spi;
import io.github.iamnicknack.pjs.device.spi.SpiConfig;
import io.github.iamnicknack.pjs.model.device.DeviceConfig;

class Pi4jSpi implements Spi {

    private final SpiConfig config;
    private final com.pi4j.io.spi.Spi spi;
    private final Runnable onClose;

    public Pi4jSpi(
            SpiConfig config,
            com.pi4j.io.spi.Spi spi,
            Runnable onClose
            ) {
        this.config = config;
        this.spi = spi;
        this.onClose = onClose;
    }

    @Override
    public int transfer(byte[] write, int writeOffset, byte[] read, int readOffset, int length) {
        return this.spi.transfer(write, writeOffset, read, readOffset, length);
    }

    @Override
    public int transfer(byte[] write, byte[] read) {
        return this.spi.transfer(write, read);
    }

    @Override
    public DeviceConfig<Spi> getConfig() {
        return config;
    }

    @Override
    public void close() {
        this.onClose.run();
    }
}
