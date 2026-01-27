package io.github.iamnicknack.pjs.logging;

import io.github.iamnicknack.pjs.device.spi.SpiTransfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static io.github.iamnicknack.pjs.logging.LoggingUtils.byteArrayAsHexString;

public class LoggingSpiTransfer implements SpiTransfer {

    private final SpiTransfer delegate;
    private final Logger logger;

    public LoggingSpiTransfer(SpiTransfer delegate, String deviceId) {
        this.delegate = delegate;
        this.logger = LoggerFactory.getLogger("device." + delegate.getClass().getSimpleName() + "::" + deviceId);
    }

    @Override
    public int transfer(Message... messages) {
        if (logger.isDebugEnabled()) {
            var str = Arrays.stream(messages)
                    .map(msg -> ">" + byteArrayAsHexString(msg.write(), 0, 32))
                    .toList();
            logger.debug(String.valueOf(str));
        }

        var result = delegate.transfer(messages);

        if (logger.isDebugEnabled()) {
            var str = Arrays.stream(messages)
                    .map(msg -> "<" + byteArrayAsHexString(msg.read(), 0, 32))
                    .toList();
            logger.debug(String.valueOf(str));
        }

        return result;
    }
}
