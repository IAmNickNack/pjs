package io.github.iamnicknack.pjs.ffm.device;

import io.github.iamnicknack.pjs.device.spi.Spi;
import io.github.iamnicknack.pjs.device.spi.SpiConfig;
import io.github.iamnicknack.pjs.device.spi.SpiProvider;
import io.github.iamnicknack.pjs.device.spi.SpiTransfer;
import io.github.iamnicknack.pjs.ffm.context.NativeContext;
import io.github.iamnicknack.pjs.ffm.device.context.FileOperations;
import io.github.iamnicknack.pjs.ffm.device.context.FileOperationsImpl;
import io.github.iamnicknack.pjs.ffm.device.context.IoctlOperationsImpl;
import io.github.iamnicknack.pjs.ffm.device.context.spi.SpiConstants;
import io.github.iamnicknack.pjs.logging.LoggingSpi;

public class NativeSpiProvider implements SpiProvider {

    private final NativeContext nativeContext;
    private final FileOperations fileOperations;

    public NativeSpiProvider(NativeContext context) {
        this.nativeContext = context;
        this.fileOperations = new FileOperationsImpl(nativeContext);
    }

    @Override
    public Spi create(SpiConfig config) {
        var path = "/dev/spidev" + config.bus() + "." + config.chipSelect();
        var spiFileDescriptor = fileOperations.open(path, FileOperationsImpl.Flags.O_RDWR);
        var ioctl = new IoctlOperationsImpl(nativeContext);

        ioctl.ioctl(spiFileDescriptor, SpiConstants.SPI_IOC_RD_MODE, config.mode());
        ioctl.ioctl(spiFileDescriptor, SpiConstants.SPI_IOC_WR_MODE, config.mode());
        ioctl.ioctl(spiFileDescriptor, SpiConstants.SPI_IOC_RD_BITS_PER_WORD, 8);
        ioctl.ioctl(spiFileDescriptor, SpiConstants.SPI_IOC_WR_BITS_PER_WORD, 8);
        ioctl.ioctl(spiFileDescriptor, SpiConstants.SPI_IOC_RD_MAX_SPEED_HZ, config.baudRate());
        ioctl.ioctl(spiFileDescriptor, SpiConstants.SPI_IOC_WR_MAX_SPEED_HZ, config.baudRate());
        ioctl.ioctl(spiFileDescriptor, SpiConstants.SPI_IOC_RD_LSB_FIRST, config.lsbFirst() ? 1 : 0);
        ioctl.ioctl(spiFileDescriptor, SpiConstants.SPI_IOC_WR_LSB_FIRST, config.lsbFirst() ? 1 : 0);

        return new NativeSpi(config, nativeContext, fileOperations.createFileDescriptor(spiFileDescriptor));
    }

    @Override
    public SpiTransfer createTransfer(Spi spi) {
        var delegateSpi = (spi instanceof LoggingSpi loggingSpi) ? loggingSpi.getDelegate() : spi;
        return ((NativeSpi)delegateSpi).new Transfer();
    }
}
