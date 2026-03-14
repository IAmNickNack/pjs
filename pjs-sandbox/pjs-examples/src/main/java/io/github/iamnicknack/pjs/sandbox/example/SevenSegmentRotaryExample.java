package io.github.iamnicknack.pjs.sandbox.example;

import io.github.iamnicknack.pjs.device.gpio.GpioEventMode;
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.device.gpio.GpioPortMode;
import io.github.iamnicknack.pjs.device.i2c.I2CConfig;
import io.github.iamnicknack.pjs.device.pwm.PwmConfig;
import io.github.iamnicknack.pjs.device.spi.SpiConfig;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;
import io.github.iamnicknack.pjs.model.event.GpioChangeEventType;
import io.github.iamnicknack.pjs.sandbox.device.FourBySevenDisplay;
import io.github.iamnicknack.pjs.sandbox.device.RotaryEncoder;
import io.github.iamnicknack.pjs.sandbox.device.RotaryEncoderButton;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.DisplayOperations;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.DrawingOperations;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.Sh1106Operations;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.buffer.BufferedDisplayOperations;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.impl.DefaultDrawingOperations;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.impl.DefaultSh1106Driver;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.impl.DirtyTrackingDisplayBuffer;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.impl.StackedDisplayBuffer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class SevenSegmentRotaryExample implements Runnable {

    private static final GpioPortConfig RESET_PIN_CONFIG = GpioPortConfig.builder()
            .pin(6)
            .portMode(GpioPortMode.OUTPUT)
            .build();

    private static final PwmConfig PWM_CONFIG = PwmConfig.builder()
            .chip(0)
            .channel(2)
            .frequency(220)
            .dutyRatio(0.5)
            .build();

    private static final SpiConfig SPI_CONFIG = SpiConfig.builder()
            .baudRate(10_000_000)
            .mode(0)
            .bus(1)
            .chipSelect(0)
            .build();

    private static final GpioPortConfig ROTARY_CONFIG = GpioPortConfig.builder()
            .id("ROTARY")
            .pin(24, 25)
            .portMode(GpioPortMode.INPUT_PULLUP)
            .eventMode(GpioEventMode.FALLING)
            .debounceDelay(6000)
            .build();

    private static final GpioPortConfig ROTARY_BUTTON_CONFIG = GpioPortConfig.builder()
            .id("ROTARY-BUTTON")
            .pin(5)
            .portMode(GpioPortMode.INPUT_PULLUP)
            .eventMode(GpioEventMode.BOTH)
            .debounceDelay(6000)
            .build();

    private static final I2CConfig I2C_CONFIG = I2CConfig.builder()
            .bus(1)
            .build();

    private final RotaryEncoder rotaryEncoder;
    private final RotaryEncoderButton rotaryButton;
    private final FourBySevenDisplay display;

    private final Sh1106Operations sh1106Operations;
    private final DrawingDisplay drawingDisplay;

    public SevenSegmentRotaryExample(DeviceRegistry registry) {
        var rotaryPort = registry.create(ROTARY_CONFIG);
        var rotaryButtonPort = registry.create(ROTARY_BUTTON_CONFIG);
        this.rotaryEncoder = new RotaryEncoder(rotaryPort);
        this.rotaryButton = new RotaryEncoderButton(rotaryEncoder, rotaryButtonPort);

        var spi = registry.create(SPI_CONFIG);
        var resetPin = registry.create(RESET_PIN_CONFIG).pin();
        var pwm = registry.create(PWM_CONFIG);
        this.display = new FourBySevenDisplay(spi, resetPin, pwm);

        var i2c = registry.create(I2C_CONFIG);
        var sh1106 = new DefaultSh1106Driver(i2c, 0x3c);
        this.sh1106Operations = new Sh1106Operations(sh1106);
        this.drawingDisplay = new DrawingDisplay(sh1106Operations);
    }

    @Override
    public void run() {
        sh1106Operations.init();
        sh1106Operations.clear();
        sh1106Operations.displayOn();

        display.reset();
        display.on();

        var drawOperation = new AtomicReference<>(DrawingDisplay.DrawOperation.NOOP);

        var countDownLatch = new CountDownLatch(1);
        rotaryEncoder.addListener(event -> {
            drawingDisplay.erase(drawOperation.get());

            var value = event.port().read();
            display.write(value);
            if (value > 50) {
                countDownLatch.countDown();
            }

            drawingDisplay.draw(drawOperation.updateAndGet(op -> drawOperation(value)));
        });

        rotaryButton.addListener(event -> {
            if (event.eventType() == GpioChangeEventType.FALLING) {
                var value = event.port().read();
                System.out.println(value);
                display.off();
            } else {
                display.on();
            }
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        display.off();
        sh1106Operations.displayOff();
    }

    DrawingDisplay.DrawOperation drawOperation(int value) {
        return drawingOperations -> drawingOperations.fillCircle(64 + value, 28, 4);
    }

    static class DrawingDisplay {
        private final StackedDisplayBuffer displayBuffer = new StackedDisplayBuffer();
        private final DirtyTrackingDisplayBuffer.Factory trackingFactory = DirtyTrackingDisplayBuffer.Factory.newDefault(8);
        private final BufferedDisplayOperations trackingBuffer = trackingFactory.create(displayBuffer);

        private final BufferedDisplayOperations additiveDisplay = trackingFactory.create(displayBuffer.additive());
        private final BufferedDisplayOperations subtractiveDisplay = trackingFactory.create(displayBuffer.subtractive());
        private final DrawingOperations drawingOperations = new DefaultDrawingOperations(additiveDisplay);
        private final DrawingOperations eraseOperations = new DefaultDrawingOperations(subtractiveDisplay);

        private final DisplayOperations deviceOperations;

        public DrawingDisplay(DisplayOperations deviceOperations) {
            this.deviceOperations = deviceOperations;
        }

        synchronized void draw(DrawOperation operation) {
            operation.draw(drawingOperations);
            trackingBuffer.copyTo(deviceOperations);
        }

        synchronized void erase(DrawOperation operation) {
            operation.draw(eraseOperations);
        }

        @FunctionalInterface
        interface DrawOperation {
            void draw(DrawingOperations drawingOperations);

            DrawOperation NOOP = _ -> {};
        }
    }
}
