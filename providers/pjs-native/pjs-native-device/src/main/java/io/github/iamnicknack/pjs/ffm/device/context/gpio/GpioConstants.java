package io.github.iamnicknack.pjs.ffm.device.context.gpio;

import static io.github.iamnicknack.pjs.ffm.device.context.IoctlConstants._IOR;
import static io.github.iamnicknack.pjs.ffm.device.context.IoctlConstants._IOWR;

/**
 * @see <a href="https://github.com/torvalds/linux/blob/master/include/uapi/linux/gpio.h">gpio.h</a>
 */
public class GpioConstants {

    public final static int GPIOHANDLES_MAX = 64;

    public static final long GPIO_GET_CHIPINFO_IOCTL = _IOR(0xB4, 0x01, 68);

    public static final long GPIO_V2_GET_LINEINFO_IOCTL = _IOWR(0xb4, 0x05, LineInfo.LAYOUT.byteSize());
    public static final long GPIO_V2_GET_LINEINFO_WATCH_IOCTL = _IOWR(0xb4, 0x06, LineInfo.LAYOUT.byteSize());
    public static final long GPIO_V2_GET_LINE_IOCTL = _IOWR(0xb4, 0x07, LineRequest.LAYOUT.byteSize());
    public static final long GPIO_V2_LINE_SET_CONFIG_IOCTL = _IOWR(0xb4, 0x0d, LineConfig.LAYOUT.byteSize());
    public static final long GPIO_V2_LINE_GET_VALUES_IOCTL = _IOWR(0xb4, 0x0e, LineValues.LAYOUT.byteSize());
    public static final long GPIO_V2_LINE_SET_VALUES_IOCTL = _IOWR(0xb4, 0x0f, LineValues.LAYOUT.byteSize());
}
