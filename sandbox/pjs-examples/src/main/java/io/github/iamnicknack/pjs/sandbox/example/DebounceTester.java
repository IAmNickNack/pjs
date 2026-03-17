package io.github.iamnicknack.pjs.sandbox.example;

import io.github.iamnicknack.pjs.device.gpio.GpioEventMode;
import io.github.iamnicknack.pjs.device.gpio.GpioPort;
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.device.gpio.GpioPortMode;
import io.github.iamnicknack.pjs.device.spi.SpiConfig;
import io.github.iamnicknack.pjs.device.spi.SpiTransfer;
import io.github.iamnicknack.pjs.device.spi.SpiTransferProvider;
import io.github.iamnicknack.pjs.model.WriteOperation;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;
import io.github.iamnicknack.pjs.model.event.GpioEventListener;
import io.github.iamnicknack.pjs.model.port.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class DebounceTester implements Runnable {

    private static final int DEBOUNCE_DELAY = 700;
    public static final int BOUNCE_PERIOD = 1000;

    private static final GpioPortConfig INPUT_CONFIG = GpioPortConfig.builder()
            .id("GPIO-INPUT")
            .pin(23)
            .portMode(GpioPortMode.INPUT)
            .eventMode(GpioEventMode.FALLING)
            .debounceDelay(DEBOUNCE_DELAY)
            .build();

    private static final SpiConfig PICO_SPI_CONFIG = SpiConfig.builder()
            .bus(1)
            .chipSelect(1)
            .baudRate(1_000_000)
            .build();

    private final Logger logger = LoggerFactory.getLogger(DebounceTester.class);
    private final DeviceRegistry deviceRegistry;
    private final SpiTransferProvider spiTransferProvider;

    public DebounceTester(DeviceRegistry deviceRegistry) {
        this.deviceRegistry = deviceRegistry;
        this.spiTransferProvider = (SpiTransferProvider)deviceRegistry.getProvider(SpiConfig.class);
    }

    @Override
    public void run() {
        try (var executor = Executors.newSingleThreadExecutor()) {
            var spi = deviceRegistry.create(PICO_SPI_CONFIG);
            var spiTransfer = spiTransferProvider.createTransfer(spi);

            var debounceDurationRegister = new IntegerRegister(0x01, spiTransfer);
            var armRegister = new VoidRegister(0x02, spi);

            var inputPort = deviceRegistry.create(INPUT_CONFIG);
            var eventCounter = new AtomicInteger(0);

            GpioEventListener<GpioPort> listener = _ -> {
                eventCounter.incrementAndGet();
                executor.execute(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    armRegister.write();
                    debounceDurationRegister.write(BOUNCE_PERIOD);
                });
            };

            inputPort.addListener(listener);

            armRegister.write();
            debounceDurationRegister.write(BOUNCE_PERIOD);

            AtomicInteger iterationCounter = new AtomicInteger(0);
            while (eventCounter.get() < 5) {
                iterationCounter.incrementAndGet();
                logger.info("Current count: {}", eventCounter.get());
                Thread.sleep(1000);
            }

            inputPort.removeListener(listener);
            executor.shutdown();
            executor.awaitTermination(2, java.util.concurrent.TimeUnit.SECONDS);

            logger.info("Final event count: {}, iteration count: {}", eventCounter.get(), iterationCounter.get());
            assert iterationCounter.get() == eventCounter.get();

        } catch (Exception e) {
            logger.error("Error occurred", e);
        }
    }

    private static class VoidRegister implements WriteOperation<Void> {
        private final int register;
        private final SerialPort delegate;

        public VoidRegister(int register, SerialPort delegate) {
            this.register = register;
            this.delegate = delegate;
        }

        @Override
        public void write(Void value) {
            write();
        }

        public void write() {
            delegate.writeByte((byte)register);
        }
    }

    private static class IntegerRegister implements WriteOperation<Integer> {
        private final SpiTransfer delegate;
        private final SpiTransfer.Message registerMessage;

        public IntegerRegister(int register, SpiTransfer delegate) {
            this.delegate = delegate;
            this.registerMessage = new SpiTransfer.DefaultMessage(
                    new byte[] { (byte) register }, 0,
                    new byte[1], 0,
                    1,
                    0,
                    true
            );
        }

        @Override
        public void write(Integer value) {
            delegate.transfer(
                    registerMessage,
                    SpiTransfer.Message.write(
                            ByteBuffer.allocate(4)
                                    .order(ByteOrder.LITTLE_ENDIAN)
                                    .putInt(value)
                                    .array()
                    )
            );
        }
    }
}

