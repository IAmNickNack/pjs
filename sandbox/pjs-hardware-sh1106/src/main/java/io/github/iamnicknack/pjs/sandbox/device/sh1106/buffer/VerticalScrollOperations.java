package io.github.iamnicknack.pjs.sandbox.device.sh1106.buffer;

import static io.github.iamnicknack.pjs.sandbox.device.sh1106.DisplayOperations.PAGE_SIZE;

public interface VerticalScrollOperations {

    /**
     * Scroll the display by the given number of pixels.
     * @param pixels The number of pixels to scroll.
     * @return data which has been scrolled off the display.
     */
    default byte[] scrollVertically(int pixels) {
        return scrollVertically(pixels, new byte[PAGE_SIZE]);
    }

    /**
     * Scroll the display by the given number of pixels with an initial porch to scroll in.
     * @param pixels The number of pixels to scroll.
     * @param initialPorch The initial porch to scroll in.
     * @return data which has been scrolled off the display.
     */
    byte[] scrollVertically(int pixels, byte[] initialPorch);

    /**
     * Compose multiple vertical scroll operations into a single operation.
     * @param operations The operations to compose.
     * @return A single vertical scroll operation that applies all the given operations in sequence.
     */
    static VerticalScrollOperations composed(VerticalScrollOperations... operations) {
        return (pixels, initialPorch) -> {
            byte[] carry = initialPorch;
            for (VerticalScrollOperations operation : operations) {
                carry = operation.scrollVertically(pixels, carry);
            }
            return carry;
        };
    }
}
