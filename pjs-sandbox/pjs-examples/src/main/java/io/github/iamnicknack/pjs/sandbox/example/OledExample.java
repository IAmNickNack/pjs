package io.github.iamnicknack.pjs.sandbox.example;

import io.github.iamnicknack.pjs.device.i2c.I2CConfig;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.Sh1106Driver;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.Sh1106Operations;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.TextOperations;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.impl.DefaultDrawingOperations;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.impl.DirtyTrackingDisplayBuffer;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.impl.StackedDisplayBuffer;

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

        runStacked();
    }

    void runSimple() {
        var dirtyTrackingDisplayBuffer = new DirtyTrackingDisplayBuffer(16);
        var textOperations = TextOperations.create(dirtyTrackingDisplayBuffer);
        textOperations.drawText(4, 0, "012345678901234567890");

        dirtyTrackingDisplayBuffer.copyTo(deviceOperations);
    }

    void runStacked() {
        var stackedBuffer = new StackedDisplayBuffer();
        var trackingBufferFactory = DirtyTrackingDisplayBuffer.Factory.newDefault(8);

        var additiveDisplayOperations = trackingBufferFactory.create(stackedBuffer.additive());
        var subtractiveDisplayOperations = trackingBufferFactory.create(stackedBuffer.subtractive());

        // Display text across the width of the screen. First, write to the buffer
        var textOperations = TextOperations.create(additiveDisplayOperations);
        textOperations.drawText(4, 0, "012345678901234567890");
        // Sync the buffer with the display
        additiveDisplayOperations.copyTo(deviceOperations);

        // Additive drawing operations to draw over the text
        var additiveDrawOperations = new DefaultDrawingOperations(additiveDisplayOperations);
        // Subtractive drawing operations to remove the drawn data without affecting the text
        var subtractiveDrawOperations = new DefaultDrawingOperations(subtractiveDisplayOperations);

        // Move a bar across the text
        for (int i = 0; i < 1024; i++) {
            additiveDrawOperations.drawLine(i, 28, i, 44);
            additiveDrawOperations.drawLine(i+1, 28, i+1, 44);
            additiveDisplayOperations.copyTo(deviceOperations);
            subtractiveDrawOperations.drawLine(i, 28, i, 44);
            subtractiveDrawOperations.drawLine(i+1, 28, i+1, 44);
        }
        additiveDisplayOperations.copyTo(deviceOperations);
    }
}
