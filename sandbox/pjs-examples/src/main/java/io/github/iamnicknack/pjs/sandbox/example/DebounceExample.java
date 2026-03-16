package io.github.iamnicknack.pjs.sandbox.example;


import io.github.iamnicknack.pjs.device.gpio.GpioEventMode;
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.device.gpio.GpioPortMode;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;
import io.github.iamnicknack.pjs.model.event.GpioChangeEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class DebounceExample implements Runnable {

    private static final GpioPortConfig INPUT_CONFIG = GpioPortConfig.builder()
            .id("GPIO-INPUT")
            .pin(23)
            .portMode(GpioPortMode.INPUT_PULLDOWN)
            .eventMode(GpioEventMode.RISING)
            .debounceDelay(20_000)
            .build();

    private final Logger logger = LoggerFactory.getLogger(DebounceExample.class);
    private final DeviceRegistry deviceRegistry;

    public DebounceExample(DeviceRegistry deviceRegistry) {
        this.deviceRegistry = deviceRegistry;
    }

    @Override
    public void run() {
        try {
            var port = deviceRegistry.create(INPUT_CONFIG);
            var count = new AtomicInteger(0);
            port.addListener(event -> {
                if (event.eventType() == GpioChangeEventType.RISING) {
                    count.incrementAndGet();
                }
            });

            while (count.get() < 5) {
                logger.info("Current count: {}", count);
                Thread.sleep(1000);
            }
            logger.info("Final count: {}", count);
        } catch (Exception e) {
            logger.error("Error", e);
        }
    }
}
