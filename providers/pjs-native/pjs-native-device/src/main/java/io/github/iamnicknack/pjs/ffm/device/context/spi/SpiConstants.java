package io.github.iamnicknack.pjs.ffm.device.context.spi;

import static io.github.iamnicknack.pjs.ffm.device.context.IoctlConstants.*;

@SuppressWarnings("unused")
public class SpiConstants {

    public static final long SPI_IOC_MAGIC = 'k';
    public static final long SPI_IOC_RD_MODE = _IOR(SPI_IOC_MAGIC, 1, 1);
    public static final long SPI_IOC_WR_MODE = _IOW(SPI_IOC_MAGIC, 1, 1);
    public static final long SPI_IOC_RD_BITS_PER_WORD = _IOR(SPI_IOC_MAGIC, 3, 1);
    public static final long SPI_IOC_WR_BITS_PER_WORD = _IOW(SPI_IOC_MAGIC, 3, 1);
    public static final long SPI_IOC_RD_MAX_SPEED_HZ = _IOR(SPI_IOC_MAGIC, 4, 4);
    public static final long SPI_IOC_WR_MAX_SPEED_HZ = _IOW(SPI_IOC_MAGIC, 4, 4);
    public static final long SPI_IOC_RD_MODE32 = _IOR(SPI_IOC_MAGIC, 5, 4);
    public static final long SPI_IOC_WR_MODE32 = _IOW(SPI_IOC_MAGIC, 5, 4);
    public static final long SPI_IOC_RD_LSB_FIRST = _IOR(SPI_IOC_MAGIC, 2, 1);
    public static final long SPI_IOC_WR_LSB_FIRST = _IOW(SPI_IOC_MAGIC, 2, 1);
    public static final long SPI_IOC_TRANSFER_SIZE = 32;


    public static long SPI_IOC_MESSAGE(int N) {
        return _IOW(SPI_IOC_MAGIC, 0, SPI_MSGSIZE(N));
    }

    static long SPI_MSGSIZE(int N) {
        return (N * SPI_IOC_TRANSFER_SIZE) < (1 << _IOC_SIZEBITS)
                ? N * SPI_IOC_TRANSFER_SIZE
                : 0;
    }

}
