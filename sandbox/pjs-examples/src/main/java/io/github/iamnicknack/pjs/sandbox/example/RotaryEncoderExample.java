package io.github.iamnicknack.pjs.sandbox.example;

import io.github.iamnicknack.pjs.device.gpio.GpioEventMode;
import io.github.iamnicknack.pjs.device.gpio.GpioPort;
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.device.gpio.GpioPortMode;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;
import io.github.iamnicknack.pjs.sandbox.device.RotaryEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class RotaryEncoderExample implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final GpioPortConfig ROTARY_BUTTON_CONFIG = GpioPortConfig.builder()
            .id("ROTARY-BUTTON")
            .pin(5)
            .portMode(GpioPortMode.INPUT_PULLUP)
            .eventMode(GpioEventMode.FALLING)
            .debounceDelay(6000)
            .build();

    private static final GpioPortConfig ROTARY_CONFIG = GpioPortConfig.builder()
            .id("ROTARY")
            .pin(24, 25)
            .portMode(GpioPortMode.INPUT_PULLUP)
            .eventMode(GpioEventMode.FALLING)
            .debounceDelay(0)
            .build();


    private final GpioPort buttonPort;
    private final GpioPort rotaryPort;

    public RotaryEncoderExample(DeviceRegistry registry) {
        buttonPort = registry.create(ROTARY_BUTTON_CONFIG);
        rotaryPort = registry.create(ROTARY_CONFIG);
    }

    @Override
    public void run() {
        var pressed = new CountDownLatch(1);
        buttonPort.addListener(_ -> pressed.countDown());

        var rotaryEncoder = new RotaryEncoder(rotaryPort);
        rotaryEncoder.addListener(event -> {
            logger.info("Rotated: {}", event.port().read());
        });

        try {
            pressed.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        logger.info("Pressed");
    }
}
