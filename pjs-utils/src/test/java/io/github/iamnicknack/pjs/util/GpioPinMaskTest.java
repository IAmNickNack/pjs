package io.github.iamnicknack.pjs.util;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class GpioPinMaskTest {

    @Test
    void canGenerateBinaryValue() {
        var pinMask = new GpioPinMask(new int[]{0, 1, 2});
        assertThat(pinMask.getMaskString(1)).isEqualTo("-----------------------------001");
        assertThat(pinMask.getMaskString(2)).isEqualTo("-----------------------------010");
        assertThat(pinMask.getMaskString(4)).isEqualTo("-----------------------------100");
        assertThat(pinMask.getMaskString(8)).isEqualTo("-----------------------------000");
    }

    @Test
    void canGenerateBinaryValueForShiftedPins() {
        var pinMask = new GpioPinMask(new int[]{29, 30, 31});
        assertThat(pinMask.getMaskString(1)).isEqualTo("001-----------------------------");
        assertThat(pinMask.getMaskString(2)).isEqualTo("010-----------------------------");
        assertThat(pinMask.getMaskString(4)).isEqualTo("100-----------------------------");
        assertThat(pinMask.getMaskString(8)).isEqualTo("000-----------------------------");
    }

    @Test
    void canGenerateBinaryValueForNonConsecutivePins() {
        var pinMask = new GpioPinMask(new int[]{0, 1, 30});
        assertThat(pinMask.getMaskString(1)).isEqualTo("-0----------------------------01");
        assertThat(pinMask.getMaskString(2)).isEqualTo("-0----------------------------10");
        assertThat(pinMask.getMaskString(4)).isEqualTo("-1----------------------------00");
        assertThat(pinMask.getMaskString(8)).isEqualTo("-0----------------------------00");
    }

    @Test
    void canGenerateBinaryValueForNonConsecutivePinsUnsorted() {
        var pinMask = new GpioPinMask(new int[]{31, 0, 1});
        assertThat(pinMask.getMaskString(1)).isEqualTo("0-----------------------------01");
        assertThat(pinMask.getMaskString(2)).isEqualTo("0-----------------------------10");
        assertThat(pinMask.getMaskString(4)).isEqualTo("1-----------------------------00");
        assertThat(pinMask.getMaskString(8)).isEqualTo("0-----------------------------00");
    }

    @Test
    void canCreateSimpleMask() {
        var pinMask = new GpioPinMask(new int[] { 2 });
        assertThat(pinMask.getUnpackedMask()).isEqualTo(4);
        assertThat(GpioPinMask.gpioMaskFor(new int[] { 2 })).isEqualTo(4);
    }

    @Test
    void canCreateMultiPinMask() {
        var pinMask = new GpioPinMask(new int[] { 2, 4 });
        assertThat(pinMask.getUnpackedMask()).isEqualTo(20);
        assertThat(GpioPinMask.gpioMaskFor(new int[] { 2, 4 })).isEqualTo(20);
    }

    @Test
    void canMaskValue() {
        var pinMask = new GpioPinMask(0b10);

        var maskedValue = pinMask.maskValue(1);
        assertThat(maskedValue).isEqualTo(2);

        var unmaskedValue = pinMask.unmaskValue(maskedValue);
        assertThat(unmaskedValue).isEqualTo(1);
    }

    @Test
    void valuesOutOfRangeAreIgnored() {
        var pinMask = new GpioPinMask(new int[]{1});
        assertThat(pinMask.maskValue(2)).isEqualTo(0);
    }

    @Test
    void valuesPartiallyInRangeAreMasked() {
        var pinMask = new GpioPinMask(new int[]{2});
        // 1 maps to bit 2 (4)
        assertThat(pinMask.maskValue(1)).isEqualTo(4);
        // 2 maps to bit 3 (masked out)
        assertThat(pinMask.maskValue(2)).isEqualTo(0);
        // 1 maps to bit 2 (4)
        assertThat(pinMask.maskValue(3)).isEqualTo(4);
    }

    @TestFactory
    Stream<DynamicTest> maskExpectations() {
        record Expectation(int pinMask, int value, int masked, int unmasked) {
        }

        return Stream.of(
                new Expectation(0b001, 1, 1, 1), // pin 0
                new Expectation(0b001, 2, 0, 0), // pin 0
                new Expectation(0b001, 3, 1, 1), // pin 0
                new Expectation(0b010, 1, 2, 1), // pin 1
                new Expectation(0b010, 2, 0, 0), // pin 1
                new Expectation(0b100, 1, 4, 1), // pin 2
                new Expectation(0b100, 2, 0, 0), // pin 2
                new Expectation(0b100, 3, 4, 1)  // pin 2
        ).map(e -> DynamicTest.dynamicTest("0b" + Integer.toBinaryString(e.pinMask) + ": " + e.value + " -> " + e.masked, () -> {
            var pinMask = new GpioPinMask(e.pinMask);

            var maskedValue = pinMask.maskValue(e.value);
            assertThat(maskedValue).isEqualTo(e.masked);

            var unmaskedValue = pinMask.unmaskValue(maskedValue);
            assertThat(unmaskedValue).isEqualTo(e.unmasked);
        }));
    }

    @Test
    void canPackValues() {
        assertThat(GpioPinMask.packBits(new int[]{1, 2})).isEqualTo(0b11);
    }

    @TestFactory
    Stream<DynamicTest> packArrayExpectations() {
        record Expectation(int[] pins, int packed) {
        }

        return Stream.of(
                new Expectation(new int[]{0, 1}, 0b11),
                new Expectation(new int[]{1, 2}, 0b11),
                new Expectation(new int[]{0, 2}, 0b11),
                new Expectation(new int[]{0, 2, 4}, 0b111)
        ).map(e -> DynamicTest.dynamicTest(
                "pins " + Arrays.toString(e.pins) + " -> 0b" + Integer.toBinaryString(e.packed),
                () -> assertThat(GpioPinMask.packBits(e.pins)).isEqualTo(e.packed)
        ));
    }

    @TestFactory
    Stream<DynamicTest> packIntExpectations() {
        record Expectation(int pinsMask, int packed) {
        }

        return Stream.of(
                new Expectation(0b11, 0b11),
                new Expectation(0b110, 0b11),
                new Expectation(0b101, 0b11),
                new Expectation(0b10101, 0b111)
        ).map(e -> DynamicTest.dynamicTest(
                "pins 0b" + Integer.toBinaryString(e.pinsMask) + " -> 0b" + Integer.toBinaryString(e.packed),
                () -> assertThat(GpioPinMask.packBits(e.pinsMask)).isEqualTo(e.packed)
        ));
    }
}