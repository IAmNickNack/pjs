package io.github.iamnicknack.pjs.util;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Mapping operations which allow an input bit value to be mapped to an arbitrary selection of output bit values.
 * <p>
 * This can be used in conjunction with a mock output port to connect a mock input port, in a similar fashion to how
 * a physical input pin might be connected to a physical output pin.
 * </p>
 */
public class GpioPinRemapper {

    private final int[] pinMappings;
    private final int pinMask;
    private final int outputMask;

    public GpioPinRemapper(int[] pinMappings) {
        assert pinMappings.length == 32;
        this.pinMappings = pinMappings;
        this.pinMask = IntStream.range(0, 32)
                .reduce(0, (acc, index) -> (pinMappings[index] != 0) ? acc | (1 << index) : acc);
        this.outputMask = IntStream.range(0, 32)
                .reduce(0, (acc, index) -> acc | pinMappings[index]);
    }

    public int[] getPinMappings() {
        var mappings = new int[32];
        System.arraycopy(pinMappings, 0, mappings, 0, 32);
        return mappings;
    }

    public int getPinMask() {
        return pinMask;
    }

    public int getOutputMask() {
        return outputMask;
    }

    /**
     * Calculate the composite mask represented by the specified pin mask
     * @param input the pin mask to expand
     * @return the composite mask of all {@link #pinMappings} represented by the input mask
     */
    public int map(int input) {
        var remaining = input & pinMask;
        var index = 0;
        var out = 0;
        while (remaining != 0) {
            var tz = Integer.numberOfTrailingZeros(remaining);
            remaining >>>= tz;
            index += tz;

            out |= ((input & (1 << index)) != 0) ? pinMappings[index] : 0;

            remaining >>>= 1;
            index++;
        }
        return out;
    }

    /**
     * Calculate which bits would need to be active to generate the specified input value
     * @param input an expanded, composite mask
     * @return the bit mask which would need to be active to generate the specified input value
     */
    public int unmap(int input) {
        var out = 0;
        for (int i = 0; i < 32; i++) {
            if ((pinMappings[i] != 0) && (input & pinMappings[i]) == pinMappings[i]) {
                out |= (1 << i);
            }
        }
        return out;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final int[] pinMappings = new int[32];

        public Builder pin(int pin, int mappedMask) {
            pinMappings[pin] = mappedMask;
            return this;
        }

        public Builder pin(int pin, int... mappedPins) {
            int value = Arrays.stream(mappedPins)
                    .reduce(0, (a, b) -> a | (1 << b));
            return pin(pin, value);
        }

        public GpioPinRemapper build() {
            return new GpioPinRemapper(pinMappings);
        }
    }
}
