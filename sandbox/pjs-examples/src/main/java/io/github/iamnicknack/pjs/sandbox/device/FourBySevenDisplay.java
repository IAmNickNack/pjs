package io.github.iamnicknack.pjs.sandbox.device;

import io.github.iamnicknack.pjs.model.SerialWriteOperation;
import io.github.iamnicknack.pjs.model.WriteOperation;
import io.github.iamnicknack.pjs.model.pin.Pin;

public class FourBySevenDisplay implements WriteOperation<Integer> {

    private final SerialWriteOperation writeOperation;
    private final Pin resetPin;
    private final Pin onOffPin;

    public FourBySevenDisplay(SerialWriteOperation writeOperation, Pin resetPin, Pin onOffPin) {
        this.writeOperation = writeOperation;
        this.resetPin = resetPin;
        this.onOffPin = onOffPin;
    }

    public void reset() {
        resetPin.high();
        resetPin.low();
        resetPin.high();
    }

    public void on() {
        onOffPin.high();
    }

    public void off() {
        onOffPin.low();
    }

    @Override
    public void write(Integer value) {
        byte[] bytes = SegmentData.toBytes(value);
        writeOperation.writeBytes(bytes);
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

        public static final int[] SEGMENTS = new int[]{
                SEG_A,
                SEG_B,
                SEG_C,
                SEG_D,
                SEG_E,
                SEG_F,
                SEG_G,
                SEG_H
        };

        public static final int[] SEVEN_SEGMENT_DIGITS = new int[]{
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

        public static final int MINUS = SEG_G;

        public static byte[] toBytes(int value) {
            var abs = Math.abs(value);
            return new byte[]{
                    (byte) SEVEN_SEGMENT_DIGITS[(abs % 10)],
                    (byte) SEVEN_SEGMENT_DIGITS[(abs / 10) % 10],
                    (byte) SEVEN_SEGMENT_DIGITS[(abs / 100) % 10],
                    (value >= 0)
                            ? (byte) SEVEN_SEGMENT_DIGITS[(abs / 1000) % 10]
                            : (byte)MINUS
            };
        }
    }
}
