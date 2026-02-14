package io.github.iamnicknack.pjs.sandbox.example;

import io.github.iamnicknack.pjs.device.gpio.GpioEventMode;
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.device.gpio.GpioPortMode;
import io.github.iamnicknack.pjs.device.i2c.I2CConfig;
import io.github.iamnicknack.pjs.device.i2c.impl.I2CSerialPort;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;
import io.github.iamnicknack.pjs.model.port.Port;
import io.github.iamnicknack.pjs.model.port.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class DebounceTester implements Runnable {

    private static final GpioPortConfig INPUT_CONFIG = GpioPortConfig.builder()
            .id("GPIO-INPUT")
            .pin(23)
            .portMode(GpioPortMode.INPUT)
            .eventMode(GpioEventMode.BOTH)
            .debounceDelay(700)
            .build();

    private static final I2CConfig PICO_I2C_CONFIG = I2CConfig.builder()
            .bus(1)
            .build();

    private final Logger logger = LoggerFactory.getLogger(DebounceTester.class);
    private final DeviceRegistry deviceRegistry;

    public DebounceTester(DeviceRegistry deviceRegistry) {
        this.deviceRegistry = deviceRegistry;
    }

    @Override
    public void run() {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var i2c = deviceRegistry.create(PICO_I2C_CONFIG);
            var port = new I2CSerialPort(0x55, i2c);
            var register = new LongRegister(0x01, port);

            var inputPort = deviceRegistry.create(INPUT_CONFIG);
            var count = new AtomicInteger(0);

            inputPort.addListener(_ -> {
                count.incrementAndGet();
                if (count.get() < 5) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    register.write(1000L);
                }
            });

            register.write(1000L);

            while (count.get() < 5) {
                logger.info("Current count: {}", count.get());
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            logger.error("Error occurred", e);
        }
    }

    private static class LongRegister implements Port<Long> {
        private final int register;
        private final SerialPort delegate;

        public LongRegister(int register, SerialPort delegate) {
            this.register = register;
            this.delegate = delegate;
        }

        @Override
        public Long read() {
            var bytes = new byte[8];
            delegate.readBytes(bytes, 0, 8);
            var buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
            return buffer.getLong();
        }

        @Override
        public void write(Long value) {
            var buffer = ByteBuffer.allocate(9)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .put((byte)register)
                    .putLong(value);

            delegate.writeBytes(buffer.array());
        }
    }
}

