package io.github.iamnicknack.pjs.ffm.device.context;

import io.github.iamnicknack.pjs.ffm.device.context.gpio.ChipInfo;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.LineInfo;

public interface GpioOperations {
    /**
     * Fetch details about the GPIO chip
     * @param devicePath the device path
     * @return A wrapper around the native data structure
     */
    ChipInfo chipInfo(String devicePath);

    /**
     * List the GPIO lines on the specified chip
     * @param chipInfo the chip to interrogate
     * @return Available GPIO lines
     */
    Iterable<LineInfo> lines(ChipInfo chipInfo);

    /**
     * Fetch specific line info
     * @param chipInfo the chip containing the line
     * @param offset the offset of the line
     * @return GPIO line info
     */
    LineInfo lineInfo(ChipInfo chipInfo, int offset);
}
