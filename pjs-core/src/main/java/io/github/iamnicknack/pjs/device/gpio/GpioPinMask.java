package io.github.iamnicknack.pjs.device.gpio;

import java.util.Arrays;
import java.util.Iterator;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

/**
 * Utility class to convert between pin numbers and GPIO bit masks.
 */
public class GpioPinMask implements Iterable<Integer> {
    private final long mask;
    private final long packedMask;

    private GpioPinMask(long mask, long packedMask) {
        this.mask = mask;
        this.packedMask = packedMask;
    }

    /**
     * Create a mask from the given pin numbers.
     * @param offsets the pin numbers to create a mask for.
     */
    public static GpioPinMask fromOffsets(int... offsets) {
        return new GpioPinMask(mask(offsets), packOffsets(offsets));
    }

    /**
     * Create a mask from the given pin mask.
     * @param mask the pin mask to create a mask for.
     */
    public static GpioPinMask fromMask(long mask) {
        return new GpioPinMask(mask, packMask(mask));
    }

    /**
     * The mask applied to values before a value is converted to GPIO pins.
     * @return the mask
     */
    public long getPackedMask() {
        return packedMask;
    }

    /**
     * The mask as represented on GPIO pins.
     * @return the mask
     */
    public long getMask() {
        return mask;
    }

    /**
     * The offsets of the GPIO pins represented by this mask.
     * @return the offsets
     */
    public int[] offsets() {
        return stream().toArray();
    }

    /**
     * Convert a value to the GPIO pin mask format
     * @param value the value to convert
     * @return the value as represented on GPIO pins
     */
    public long maskValue(long value) {
        return unpackByMask(mask, value);
    }

    /**
     * Convert a value from the GPIO pin mask format to the original value.
     * @param value the value as represented on GPIO pins
     * @return the original value
     */
    public long unmaskValue(long value) {
        return packByMask(mask, value);
    }

    /**
     * Get a string representation of the GPIO mask for the given value.
     * @param value the unmasked value to get the mask for
     * @return a string representation of the GPIO mask
     */
    public String getMaskString(long value) {
        var masked = value & packedMask;

        var str = "--------------------------------".toCharArray();

        long remaining = mask & 0xFFFFFFFFL; // bits at absolute GPIO positions
        int packedIndex = 0;          // index in packed representation (LSB-first)
        int absPos = 0;               // current absolute bit position

        while (remaining != 0 && absPos < 32) {
            int tz = Long.numberOfTrailingZeros(remaining);
            absPos += tz;
            remaining >>>= tz; // skip zero-run
            // now LSB of remaining is a set bit at absolute position absPos
            char c = ((masked & (1L << packedIndex)) != 0) ? '1' : '0';
            str[31 - absPos] = c;
            // consume this set bit
            remaining >>>= 1;
            absPos++;
            packedIndex++;
        }

        return new String(str);
    }

    /**
     * Returns a string representation of the mask, with only the lower 32 bits considered.
     * @return the string representation
     */
    public String getMaskString() {
        return getMaskString(0xFFFFFFFFL);
    }

    @Override
    public Iterator<Integer> iterator() {
        return new OffsetsIterator(this.mask);
    }

    /**
     * Returns an {@link IntStream} of the offsets of the set bits in the mask.
     * @return the stream of offsets
     */
    public IntStream stream() {
        return stream(this.mask);
    }

    /**
     * Convert an array of pin numbers to a GPIO mask.
     * @param pins the pin numbers to convert
     * @return the GPIO mask
     */
    public static int mask(int... pins) {
        return Arrays.stream(pins)
                .reduce(0, (acc, pin) -> acc | (1 << pin));
    }

    /**
     * "pack" bits specified by the given mask. E.g. `1101` becomes `111`.
     * @param mask the value to pack
     * @return the packed value
     */
    public static long packMask(long mask) {
        long out = 0;
        int i = 0;
        long v = mask;
        while (v != 0) {
            int tz = Long.numberOfTrailingZeros(v);
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
    public static long packOffsets(int... values) {
        return packMask(mask(values));
    }

    /**
     * Returns an array of the offsets of the set bits in the mask.
     * @param mask the mask to get offsets for
     * @return the array of offsets
     */
    public static int[] offsets(long mask) {
        return stream(mask).toArray();
    }

    /**
     * Returns a stream of the offsets of the set bits in the mask.
     * @param mask the mask to get offsets for
     * @return the stream of offsets
     */
    public static IntStream stream(long mask) {
        return StreamSupport.intStream(
                Spliterators.spliteratorUnknownSize(new OffsetsIterator(mask), Spliterator.ORDERED),
                false
        );
    }

    static int packByMask(long mask, long value) {
        int out = 0;
        int i = 0; // absolute bit position in value
        int k = 0; // next bit position in packed output
        long m = mask;
        while (m != 0) {
            int tz = Long.numberOfTrailingZeros(m); // skip mask zero bits
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
    static long unpackByMask(long mask, long value) {
        long out = 0;
        int i = 0; // bit position in result
        long m = mask;
        long v = value;
        while (m != 0 && v != 0) {
            int tz = Long.numberOfTrailingZeros(m); // skip mask zero bits
            i += tz;
            m >>>= tz;
            // now mask LSB is 1 (guaranteed)
            if ((v & 1) != 0) out |= (1L << i);
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
