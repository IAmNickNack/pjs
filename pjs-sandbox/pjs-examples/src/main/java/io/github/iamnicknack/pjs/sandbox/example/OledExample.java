package io.github.iamnicknack.pjs.sandbox.example;

import io.github.iamnicknack.pjs.device.i2c.I2CConfig;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.DisplayOperations;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.DrawingOperations;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.Sh1106Driver;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.Sh1106Operations;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.TextOperations;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.impl.DefaultDisplayBuffer;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.impl.DefaultDrawingOperations;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.impl.DirtyTrackingDisplayBuffer;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.impl.LongDisplayBuffer;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.impl.StackedDisplayBuffer;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.impl.UpDownScrollOperations;

import java.util.function.BiConsumer;

public class OledExample implements Runnable {

    private static final I2CConfig I2C_CONFIG = I2CConfig.builder()
            .bus(1)
            .build();

    private final Sh1106Driver driver;
    private final Sh1106Operations deviceOperations;

    public OledExample(DeviceRegistry registry) {
        var i2c = registry.create(I2C_CONFIG);
        driver = new Sh1106Driver(i2c, 0x3c);
        deviceOperations = new Sh1106Operations(driver);
    }

    @Override
    public void run() {

        deviceOperations.init();
        deviceOperations.clear();
        deviceOperations.displayOn();

//        runStacked();
        runScroll();
//        runScrollLong();
//        runLongWithPorch();
//        runWithBallAndBox();
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

        BiConsumer<Integer, DrawingOperations> drawBar = (i, drawingOperations) -> {
//            drawingOperations.fillRectangle(i, 28, Math.min(i + 2, 255), 42);
//            if (i > 6 && i < 249)
                drawingOperations.fillCircle(i, 36, 6);
        };

        // Move a bar across the text
        for (int i = 0; i < 256; i++) {
            drawBar.accept(i, additiveDrawOperations);
            additiveDisplayOperations.copyTo(deviceOperations);
            drawBar.accept(i, subtractiveDrawOperations);
        }
        additiveDisplayOperations.copyTo(deviceOperations);
     }

    void runScroll() {
        var buffer = new DefaultDisplayBuffer();

        var trackingBufferFactory = DirtyTrackingDisplayBuffer.Factory.newDefault(8);
        var trackedOperations = trackingBufferFactory.create(buffer);
        var scrollDownOperations = UpDownScrollOperations.scrollUp(trackedOperations);
        var textOperations = TextOperations.create(trackedOperations);
        textOperations.drawText(0, 0, "012345678901234567890");

        trackedOperations.copyTo(deviceOperations);
        byte[] backPorch = new byte[DisplayOperations.PAGE_SIZE];
        for (int iteration = 0; iteration < 2; iteration++) {
            for (int i = 0; i < 116; i++) {
                backPorch = scrollDownOperations.scrollVertically(1, backPorch);
                trackedOperations.copyTo(deviceOperations);
            }
        }
    }

    void runScrollLong() {
        var buffer = new LongDisplayBuffer();

        var trackingBufferFactory = DirtyTrackingDisplayBuffer.Factory.newDefault(8);
        var trackedOperations = trackingBufferFactory.create(buffer);
        var scrollOperations = UpDownScrollOperations.scrollUp(trackedOperations);
        var textOperations = TextOperations.create(trackedOperations);
        textOperations.drawText(0, 0, "012345678901234567890");

        trackedOperations.copyTo(deviceOperations);
        byte[] backPorch = new byte[DisplayOperations.PAGE_SIZE];
        for (int iteration = 0; iteration < 2; iteration++) {
            for (int i = 0; i < 116; i++) {
                backPorch = scrollOperations.scrollVertically(1, backPorch);
                trackedOperations.copyTo(deviceOperations);
            }
        }
    }

    void runWithBallAndBox() {
        // The buffer that will be copied to the display
        // `Stacked` to allow for multiple layers of drawing
        var displayBuffer = new StackedDisplayBuffer();
        var trackingBuffer = DirtyTrackingDisplayBuffer.Factory.newDefault(8).create(displayBuffer);

        var ballBuffer = new StackedDisplayBuffer();
        var ballTrackingBufferFactory = DirtyTrackingDisplayBuffer.Factory.newDefault(8);
        var ballTrackingBuffer = ballTrackingBufferFactory.create(ballBuffer);
        var additiveBallOperations = ballTrackingBufferFactory.create(ballBuffer.additive());
        var subtractiveBallOperations = ballTrackingBufferFactory.create(ballBuffer.subtractive());
        var drawBallOperations = new DefaultDrawingOperations(additiveBallOperations);
        var eraseBallOperations = new DefaultDrawingOperations(subtractiveBallOperations);

        var boxBuffer = new DefaultDisplayBuffer();
        var boxTrackingBuffer = DirtyTrackingDisplayBuffer.Factory.newDefault(8).create(boxBuffer);
        var boxOperations = new DefaultDrawingOperations(boxTrackingBuffer);

        boxOperations.fillRectangle(10, 10, 20, 20);
        var box = boxTrackingBuffer.snapshot();

        boxTrackingBuffer.copyTo(trackingBuffer);
        trackingBuffer.copyTo(deviceOperations);

        for (int i = 0; i < 100; i++) {
            drawBallOperations.fillCircle(10 + i, 10, 5);
            trackingBuffer.copyFrom(box, ballTrackingBuffer);
            trackingBuffer.copyTo(deviceOperations);
            eraseBallOperations.fillCircle(10 + i, 10, 5);
        }
        trackingBuffer.copyTo(deviceOperations);
    }
}
