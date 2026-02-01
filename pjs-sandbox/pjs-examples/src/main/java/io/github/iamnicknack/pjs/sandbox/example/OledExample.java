package io.github.iamnicknack.pjs.sandbox.example;

import io.github.iamnicknack.pjs.device.i2c.I2CConfig;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.BufferedDisplayOperations;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.DefaultDrawingOperations;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.Sh1106Driver;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.Sh1106Operations;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.StackedDisplayBuffer;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.TextOperations;

public class OledExample implements Runnable {

    private static final I2CConfig I2C_CONFIG = I2CConfig.builder()
            .bus(1)
            .build();

    private final Sh1106Operations deviceOperations;

    public OledExample(DeviceRegistry registry) {
        var i2c = registry.create(I2C_CONFIG);
        deviceOperations = new Sh1106Operations(new Sh1106Driver(i2c, 0x3c));
    }

    @Override
    public void run() {

        deviceOperations.init();
        deviceOperations.clear();
        deviceOperations.displayOn();

        var stackedBuffer = new StackedDisplayBuffer();
        var additiveDisplayOperations = new BufferedDisplayOperations(stackedBuffer.additive(), 8);
        var subtractiveDisplayOperations = new BufferedDisplayOperations(stackedBuffer.subtractive(), 8);

        var textOperations = TextOperations.create(additiveDisplayOperations);
        textOperations.drawText(4, 0, "012345678901234567890");

        additiveDisplayOperations.copyTo(deviceOperations);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        var additiveDrawOperations = new DefaultDrawingOperations(additiveDisplayOperations);
        additiveDrawOperations.drawEllipse(0, 40, 8, 4);
        additiveDisplayOperations.copyTo(deviceOperations);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        var subtractiveDrawOperations = new DefaultDrawingOperations(subtractiveDisplayOperations);

        for (int i = 0; i < 256; i++) {
            subtractiveDrawOperations.drawEllipse(i, 40, 8, 4);
            additiveDrawOperations.drawEllipse(i + 1, 40, 8, 4);
            subtractiveDisplayOperations.copyTo(deviceOperations);
        }
    }
}
