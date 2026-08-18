package io.github.iamnicknack.pjs.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.PrimitiveIterator;

/**
 * Utility class to convert between pin numbers and GPIO bit masks.
 * @param packedMask the mask applied to values before a value is converted to GPIO pins
 * @param unpackedMask the mask as represented on GPIO pins
 */
public record GpioPinMask(int packedMask, int unpackedMask) implements Iterable<Integer> {

    /**
     * Create a mask from the given pin numbers.
     * @param pins the pin numbers to create a mask for.
     */
    public GpioPinMask(int[] pins) {
        this(packBits(pins), gpioMaskFor(pins));
    }

    /**
     * Create a mask from the given pin mask.
     * @param pinMask the pin mask to create a mask for.
     */
    public GpioPinMask(int pinMask) {
        this(packBits(pinMask), pinMask);
    }

    /**
     * Convert a value to the GPIO pin mask format
     * @param value the value to convert
     * @return the value as represented on GPIO pins
     */
    public int maskValue(int value) {
        return unpackByMask(unpackedMask, value);
    }

    /**
     * Convert a value from the GPIO pin mask format to the original value.
     * @param value the value as represented on GPIO pins
     * @return the original value
     */
    public int unmaskValue(int value) {
        return packByMask(unpackedMask, value);
    }

    /**
     * Get a string representation of the GPIO mask for the given value.
     * @param value the unmasked value to get the mask for
     * @return a string representation of the GPIO mask
     */
    public String getMaskString(int value) {
        var masked = value & packedMask;

        var str = "--------------------------------".toCharArray();

        int remaining = unpackedMask; // bits at absolute GPIO positions
        int packedIndex = 0;          // index in packed representation (LSB-first)
        int absPos = 0;               // current absolute bit position

        while (remaining != 0 && packedIndex < 32) {
            int tz = Integer.numberOfTrailingZeros(remaining);
            absPos += tz;
            remaining >>>= tz; // skip zero-run
            // now LSB of remaining is a set bit at absolute position absPos
            char c = ((masked & (1 << packedIndex)) != 0) ? '1' : '0';
            str[31 - absPos] = c;
            // consume this set bit
            remaining >>>= 1;
            absPos++;
            packedIndex++;
        }

        return new String(str);
    }

    /**
     * Returns the number of pins in this mask.
     * @return the number of pins
     */
    public int length() {
        return Integer.bitCount(unpackedMask);
    }

    /**
     * Returns the pin numbers in this mask.
     * @return the pin numbers
     */
    public int[] offsets() {
        var result = new int[Integer.bitCount(unpackedMask)];
        int i = 0;
        for (var pin : this) {
            result[i++] = pin;
        }
        return result;
    }

    @Override
    public Iterator<Integer> iterator() {
        return new OffsetsIterator(unpackedMask);
    }

    /**
     * Convert an array of pin numbers to a GPIO mask.
     * @param offsets the pin numbers to convert
     * @return the GPIO mask
     */
    public static int gpioMaskFor(int[] offsets) {
        return Arrays.stream(offsets)
                .reduce(0, (acc, pin) -> acc | (1 << pin));
    }

    /**
     * "pack" bits specified by the given mask. E.g. `1101` becomes `111`.
     * @param value the value to pack
     * @return the packed value
     */
    public static int packBits(int value) {
        int out = 0;
        int i = 0;
        int v = value;
        while (v != 0) {
            int tz = Integer.numberOfTrailingZeros(v);
            v >>>= tz;
            out |= (v & 1) << i;
            v >>>= 1;
            i++;
        }

        return out;
    }

    /**
     * Pack bits for the given array of values.
     * @param values the values to pack
     * @return the packed values
     */
    public static int packBits(int[] values) {
        return packBits(gpioMaskFor(values));
    }

    static int packByMask(int mask, int value) {
        int out = 0;
        int i = 0; // absolute bit position in value
        int k = 0; // next bit position in packed output
        while (mask != 0) {
            int tz = Integer.numberOfTrailingZeros(mask); // skip mask zero bits
            i += tz;
            mask >>>= tz;
            // now mask LSB is 1 (guaranteed)
            if (((value >>> i) & 1) != 0) out |= (1 << k);
            mask >>>= 1; // consumed this mask bit
            i++;         // move to next absolute bit position
            k++;         // advance packed output position
        }
        return out;
    }

    // Faster: skip zero runs in mask and consume `value` LSB-first by shifting it
    static int unpackByMask(int mask, int value) {
        int out = 0;
        int i = 0; // bit position in result
        while (mask != 0 && value != 0) {
            int tz = Integer.numberOfTrailingZeros(mask); // skip mask zero bits
            i += tz;
            mask >>>= tz;
            // now mask LSB is 1 (guaranteed)
            if ((value & 1) != 0) out |= (1 << i);
            value >>>= 1;  // consume LSB of value
            mask >>>= 1;   // consumed this mask bit
            i++;
        }
        return out;
    }

    /**
     * An iterator over the set bits in a mask.
     */
    private static class OffsetsIterator implements PrimitiveIterator.OfInt {
        private long remaining;

        private OffsetsIterator(long mask) {
            this.remaining = mask;
        }

        @Override
        public boolean hasNext() {
            return remaining != 0;
        }

        @Override
        public int nextInt() {
            int index = Long.numberOfTrailingZeros(remaining);
            remaining &= ~(1L << index);
            return index;
        }
    }
}
