package io.github.iamnicknack.pjs.sandbox.example;

import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.device.gpio.GpioPortMode;
import io.github.iamnicknack.pjs.device.pwm.PwmConfig;
import io.github.iamnicknack.pjs.device.spi.SpiConfig;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;
import io.github.iamnicknack.pjs.model.SerialWriteOperation;
import io.github.iamnicknack.pjs.model.WriteOperation;
import io.github.iamnicknack.pjs.model.pin.Pin;

import java.util.stream.IntStream;

public class SevenSegmentExample implements Runnable {

    private static final GpioPortConfig RESET_PIN_CONFIG = GpioPortConfig.builder()
            .pin(5)
            .portMode(GpioPortMode.OUTPUT)
            .build();


    private static final PwmConfig PWM_CONFIG = PwmConfig.builder()
            .chip(0)
            .channel(2)
            .frequency(220)
            .dutyRatio(0.5)
            .build();

    private static final SpiConfig SPI_CONFIG = SpiConfig.builder()
            .baudRate(100_000)
            .mode(0)
            .bus(1)
            .chipSelect(0)
            .build();

    private final FourBySevenDisplay display;

    public SevenSegmentExample(DeviceRegistry registry) {
        display = new FourBySevenDisplay(
                registry.create(SPI_CONFIG),
                registry.create(PWM_CONFIG),
                registry.create(RESET_PIN_CONFIG).pin()
        );
    }

    @Override
    public void run() {
        display.reset();
        display.on();

        IntStream.range(0, 1000).forEach(display::write);

        display.off();
    }

    public static class FourBySevenDisplay implements WriteOperation<Integer> {

        private final SerialWriteOperation writeOperation;
        private final Pin resetPin;
        private final Pin refreshPin;

        public FourBySevenDisplay(SerialWriteOperation writeOperation, Pin resetPin, Pin refreshPin) {
            this.writeOperation = writeOperation;
            this.resetPin = resetPin;
            this.refreshPin = refreshPin;
        }

        public void reset() {
            resetPin.high();
            resetPin.low();
            resetPin.high();
        }

        public void on() {
            refreshPin.high();
        }

        public void off() {
            refreshPin.low();
        }

        @Override
        public void write(Integer value) {
            byte[] bytes = SegmentData.toBytes(value);
            writeOperation.writeBytes(bytes);
        }
    }


    public static class SegmentData {

        public static final int SEG_A = 1;
        public static final int SEG_B = 1 << 2;
        public static final int SEG_C = 1 << 6;
        public static final int SEG_D = 1 << 4;
        public static final int SEG_E = 1 << 3;
        public static final int SEG_F = 1 << 1;
        public static final int SEG_G = 1 << 7;
        public static final int SEG_H = 1 << 5;

        public static final int[] SEGMENTS = new int[] {
                SEG_A,
                SEG_B,
                SEG_C,
                SEG_D,
                SEG_E,
                SEG_F,
                SEG_G,
                SEG_H
        };

        public static final int[] SEVEN_SEGMENT_DIGITS = new int[] {
                SEG_A | SEG_B | SEG_C | SEG_D | SEG_E | SEG_F,  // 0
                SEG_B | SEG_C,  // 1
                SEG_A | SEG_B | SEG_D | SEG_E | SEG_G,  // 2
                SEG_A | SEG_B | SEG_C | SEG_D | SEG_G,  // 3
                SEG_B | SEG_C | SEG_F | SEG_G,  // 4
                SEG_A | SEG_C | SEG_D | SEG_F | SEG_G,  // 5
                SEG_A | SEG_C | SEG_D | SEG_E | SEG_F | SEG_G,  // 6
                SEG_A | SEG_B | SEG_C,  // 7
                SEG_A | SEG_B | SEG_C | SEG_D | SEG_E | SEG_F | SEG_G,  // 8
                SEG_A | SEG_B | SEG_C | SEG_D | SEG_F | SEG_G,  // 9
                SEG_A | SEG_B | SEG_C | SEG_E | SEG_F | SEG_G,  // A
                SEG_C | SEG_D | SEG_E | SEG_F | SEG_G,  // b
                SEG_A | SEG_D | SEG_E | SEG_F,  // C
                SEG_B | SEG_C | SEG_D | SEG_E | SEG_G,  // d
                SEG_A | SEG_D | SEG_E | SEG_F | SEG_G,  // E
                SEG_A | SEG_E | SEG_F | SEG_G // F
        };

        public static byte[] toBytes(int value) {
            return new byte[] {
                    (byte) SEVEN_SEGMENT_DIGITS[(value % 10)],
                    (byte) SEVEN_SEGMENT_DIGITS[(value / 10) % 10],
                    (byte) SEVEN_SEGMENT_DIGITS[(value / 100) % 10],
                    (byte) SEVEN_SEGMENT_DIGITS[(value / 1000) % 10]
            };
        }
    }
}
