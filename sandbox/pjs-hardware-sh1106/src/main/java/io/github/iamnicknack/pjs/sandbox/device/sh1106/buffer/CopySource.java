package io.github.iamnicknack.pjs.sandbox.device.sh1106.buffer;

import io.github.iamnicknack.pjs.sandbox.device.sh1106.DisplayOperations;

/**
 * Represents an operation that can be applied to a display. Name may change
 */
public interface CopySource {
    /**
     * Apply this operation to the given display.
     * @param target The display to apply the operation to.
     */
    void copyTo(DisplayOperations target);
}
