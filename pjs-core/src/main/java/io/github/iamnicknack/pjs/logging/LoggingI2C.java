package io.github.iamnicknack.pjs.logging;

import io.github.iamnicknack.pjs.device.i2c.I2C;
import io.github.iamnicknack.pjs.model.device.DeviceConfig;
import io.github.iamnicknack.pjs.model.device.WithDelegateDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static io.github.iamnicknack.pjs.logging.LoggingUtils.byteArrayAsHexString;

public class LoggingI2C implements I2C, WithDelegateDevice<I2C> {

    private final I2C delegate;
    private final Logger logger;
    private final int maxLogLength;

    public LoggingI2C(I2C delegate) {
        this(delegate, 32);
    }

    public LoggingI2C(I2C delegate, int maxLogLength) {
        this.delegate = delegate;
        this.logger = LoggerFactory.getLogger("device." + delegate.getClass().getSimpleName() + "::" + delegate.getConfig().getId());
        this.maxLogLength = maxLogLength;
    }

    @Override
    public void transfer(Message[] messages) {
        if (messages.length == 0) return;

        delegate.transfer(messages);

        var str = Arrays.stream(messages)
                .map(msg -> String.format("%s0x%02x: %s",
                        (msg.type() == Message.Type.READ ? "<" : ">"),
                        msg.address(),
                        byteArrayAsHexString(msg.data(), 0, Math.min(maxLogLength, msg.data().length))
                ))
                .toList();

        logger.debug(String.valueOf(str));
    }

    @Override
    public DeviceConfig<I2C> getConfig() {
        return delegate.getConfig();
    }

    public I2C getDelegate() {
        return delegate;
    }
}
