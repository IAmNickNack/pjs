package io.github.iamnicknack.pjs.sandbox.example;

import io.github.iamnicknack.pjs.device.gpio.GpioPort;
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.device.gpio.GpioPortMode;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;
import io.github.iamnicknack.pjs.model.event.GpioEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GpioExample implements Runnable {

    private static final GpioPortConfig INPUT_CONFIG = GpioPortConfig.builder()
            .id("GPIO-INPUT")
            .pin(22)
            .portMode(GpioPortMode.INPUT)
            .build();

    private static final GpioPortConfig OUTPUT_CONFIG = GpioPortConfig.builder()
            .id("GPIO-OUTPUT")
            .pin(1, 27)
            .portMode(GpioPortMode.OUTPUT)
            .build();

    private static final int DELAY = 250;

    private final Logger logger = LoggerFactory.getLogger(GpioExample.class);
    private final DeviceRegistry deviceRegistry;

    public GpioExample(DeviceRegistry deviceRegistry) {
        this.deviceRegistry = deviceRegistry;
    }

    @Override
    public void run() {

        try(var inputPort = deviceRegistry.create(INPUT_CONFIG);
            var outputPort = deviceRegistry.create(OUTPUT_CONFIG)) {

            GpioEventListener<GpioPort> listener = value -> logger.info("GPIO-OUTPUT: {}", value);
            inputPort.addListener(listener);

            outputPort.write(1);
            Thread.sleep(DELAY);
            outputPort.write(2);
            Thread.sleep(DELAY);
            outputPort.write(3);
            Thread.sleep(DELAY);
            outputPort.write(0);
            Thread.sleep(DELAY);

            var pin = outputPort.pin(1, 2);
            pin.high();
            Thread.sleep(DELAY);
            pin.low();
            Thread.sleep(DELAY);
            pin.high();
            Thread.sleep(DELAY);
            pin.low();
            Thread.sleep(DELAY);

            outputPort.write(0);

            inputPort.removeListener(listener);

        } catch (Exception e) {
            logger.error("Error", e);
        }

    }
}
