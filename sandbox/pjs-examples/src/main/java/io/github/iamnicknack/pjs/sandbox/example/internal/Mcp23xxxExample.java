package io.github.iamnicknack.pjs.sandbox.example.internal;

import io.github.iamnicknack.pjs.sandbox.device.mcp.Mcp23x08;
import io.github.iamnicknack.pjs.model.pin.Pin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mcp23xxxExample implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Mcp23x08 device;
    private final Pin resetPin;

    public Mcp23xxxExample(Mcp23x08 device, Pin resetPin) {
        this.device = device;
        this.resetPin = resetPin;
    }

    @Override
    public void run() {
        try {
            resetPin.high();

            device.iodir.write(0x00);
            device.olat.write(0x55);

            Thread.sleep(500);
            device.olat.write(0xAA);
            Thread.sleep(500);
            device.olat.write(0x55);
            Thread.sleep(500);
            device.olat.write(0xAA);

            logger.info("DDR value:     {}", String.format("%02x", device.iodir.read()));
            logger.info("Latch value:   {}", String.format("%02x", device.olat.read() & 0xff));

            resetPin.low();
        } catch (InterruptedException e) {
            logger.error("Interrupted", e);
        }
    }
}
