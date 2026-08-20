package io.github.iamnicknack.pjs.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.PrimitiveIterator;

/**
 * Utility class to convert between pin numbers and GPIO bit masks.
 */
public class GpioPinMask implements Iterable<Integer> {
    private final int packedMask;
    private final int unpackedMask;

    private GpioPinMask(int packedMask, int unpackedMask) {
        this.packedMask = packedMask;
        this.unpackedMask = unpackedMask;
    }

    /**
     * Create a mask from the given pin numbers.
     * @param offsets the pin numbers to create a mask for.
     */
    public static GpioPinMask fromOffsets(int... offsets) {
        return new GpioPinMask(packBits(offsets), gpioMaskFor(offsets));
    }

    /**
     * Create a mask from the given pin mask.
     * @param mask the pin mask to create a mask for.
     */
    public static GpioPinMask fromMask(int mask) {
        return new GpioPinMask(packBits(mask), mask);
    }

    /**
     * The mask applied to values before a value is converted to GPIO pins.
     * @return the mask
     */
    public int getPackedMask() {
        return packedMask;
    }

    /**
     * The mask as represented on GPIO pins.
     * @return the mask
     */
    public int getUnpackedMask() {
        return unpackedMask;
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

    @Override
    public Iterator<Integer> iterator() {
        return new OffsetsIterator(this.unpackedMask);
    }

    /**
     * Convert an array of pin numbers to a GPIO mask.
     * @param pins the pin numbers to convert
     * @return the GPIO mask
     */
    public static int gpioMaskFor(int... pins) {
        return Arrays.stream(pins)
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
    public static int packBits(int... values) {
        return packBits(gpioMaskFor(values));
    }

    static int packByMask(int mask, int value) {
        int out = 0;
        int i = 0; // absolute bit position in value
        int k = 0; // next bit position in packed output
        int m = mask;
        while (m != 0) {
            int tz = Integer.numberOfTrailingZeros(m); // skip mask zero bits
            i += tz;
            m >>>= tz;
            // now mask LSB is 1 (guaranteed)
            if (((value >>> i) & 1) != 0) out |= (1 << k);
            m >>>= 1; // consumed this mask bit
            i++;         // move to next absolute bit position
            k++;         // advance packed output position
        }
        return out;
    }

    // Faster: skip zero runs in mask and consume `value` LSB-first by shifting it
    static int unpackByMask(int mask, int value) {
        int out = 0;
        int i = 0; // bit position in result
        int m = mask;
        int v = value;
        while (m != 0 && v != 0) {
            int tz = Integer.numberOfTrailingZeros(m); // skip mask zero bits
            i += tz;
            m >>>= tz;
            // now mask LSB is 1 (guaranteed)
            if ((v & 1) != 0) out |= (1 << i);
            v >>>= 1;  // consume LSB of value
            m >>>= 1;   // consumed this mask bit
            i++;
        }
        return out;
    }

    /**
     * Iterator for the offsets of the set bits in the mask.
     */
    private static class OffsetsIterator implements PrimitiveIterator.OfInt {

        private long remaining;

        public OffsetsIterator(long remaining) {
            this.remaining = remaining;
        }

        @Override
        public int nextInt() {
            var index = Long.numberOfTrailingZeros(remaining);
            remaining &= ~(1L << index);
            return index;
        }

        @Override
        public boolean hasNext() {
            return remaining != 0L;
        }
    }
}
