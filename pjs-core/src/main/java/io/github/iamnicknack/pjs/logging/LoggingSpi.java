package io.github.iamnicknack.pjs.logging;

import io.github.iamnicknack.pjs.device.spi.Spi;
import io.github.iamnicknack.pjs.model.device.DeviceConfig;
import io.github.iamnicknack.pjs.model.device.WithDelegateDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.github.iamnicknack.pjs.logging.LoggingUtils.byteArrayAsHexString;

public class LoggingSpi implements Spi, WithDelegateDevice<Spi> {

    private final Spi delegate;
    private final Logger logger;

    public LoggingSpi(Spi delegate) {
        this.delegate = delegate;
        this.logger = LoggerFactory.getLogger("device." + delegate.getClass().getSimpleName() + "::" + delegate.getConfig().getId());
    }

    @Override
    public int transfer(byte[] write, int writeOffset, byte[] read, int readOffset, int length) {
        if (logger.isDebugEnabled()) {
            logger.debug("Sending : {} bytes: {}", length, byteArrayAsHexString(write, writeOffset, length));
        }
        var result = delegate.transfer(write, writeOffset, read, readOffset, length);
        if (logger.isDebugEnabled()) {
            logger.debug("Received: {} bytes: {}", length, byteArrayAsHexString(read, readOffset, length));
        }
        return result;
    }

    @Override
    public DeviceConfig<Spi> getConfig() {
        return delegate.getConfig();
    }

    @Override
    public Spi getDelegate() {
        return delegate;
    }

    @Override
    public void close() throws Exception {
        delegate.close();
    }
}
