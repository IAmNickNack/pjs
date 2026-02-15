package io.github.iamnicknack.pjs.ffm.device;

import io.github.iamnicknack.pjs.device.i2c.I2C;
import io.github.iamnicknack.pjs.device.i2c.I2CConfig;
import io.github.iamnicknack.pjs.device.i2c.I2CProvider;
import io.github.iamnicknack.pjs.ffm.context.NativeContext;
import io.github.iamnicknack.pjs.ffm.device.context.FileOperations;
import io.github.iamnicknack.pjs.ffm.device.context.FileOperationsImpl;
import io.github.iamnicknack.pjs.ffm.device.context.IoctlOperations;
import io.github.iamnicknack.pjs.ffm.device.context.IoctlOperationsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.github.iamnicknack.pjs.ffm.device.context.i2c.I2CConstants.I2C_CHECK_FUNCTIONALITY;
import static io.github.iamnicknack.pjs.ffm.device.context.i2c.I2CConstants.I2C_FUNC_I2C;

public class NativeI2CProvider implements I2CProvider {

    private static final String I2C_BUS = "/dev/i2c-";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final FileOperations fileOperations;
    private final IoctlOperations ioctlOperations;

    public NativeI2CProvider(NativeContext nativeContext) {
        this(new FileOperationsImpl(nativeContext), new IoctlOperationsImpl(nativeContext));
    }

    public NativeI2CProvider(
            FileOperations fileOperations,
            IoctlOperations ioctlOperations
    ) {
        this.fileOperations = fileOperations;
        this.ioctlOperations = ioctlOperations;
    }

    @Override
    public I2C create(I2CConfig config) {
        logger.info("Creating I2C bus: {}", config.bus());

        var path = I2C_BUS + config.bus();
        var fileDescriptor = fileOperations.openFd(path, FileOperationsImpl.Flags.O_RDONLY);

        var functions = ioctlOperations.ioctl(fileDescriptor, I2C_CHECK_FUNCTIONALITY, 0);
        var directModeSupported = (functions & I2C_FUNC_I2C) != 0;

        if (!directModeSupported) {
            fileDescriptor.close();
            throw new IllegalStateException("I2C bus " + config.bus() + " does not support direct portMode.");
        }

        return new NativeI2C(config, ioctlOperations, fileDescriptor);
    }
}
