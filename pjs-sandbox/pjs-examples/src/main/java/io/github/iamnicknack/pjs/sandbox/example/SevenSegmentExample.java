package io.github.iamnicknack.pjs.sandbox.example;

import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.device.gpio.GpioPortMode;
import io.github.iamnicknack.pjs.device.pwm.PwmConfig;
import io.github.iamnicknack.pjs.device.spi.SpiConfig;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;
import io.github.iamnicknack.pjs.sandbox.device.FourBySevenDisplay;

import java.util.stream.IntStream;

public class SevenSegmentExample implements Runnable {

    private static final GpioPortConfig RESET_PIN_CONFIG = GpioPortConfig.builder()
            .pin(6)
            .portMode(GpioPortMode.OUTPUT)
            .build();

    private static final PwmConfig PWM_CONFIG = PwmConfig.builder()
            .chip(0)
            .channel(2)
            .frequency(220)
            .dutyRatio(0.5)
            .build();

    private static final SpiConfig SPI_CONFIG = SpiConfig.builder()
            .baudRate(10_000_000)
            .mode(0)
            .bus(1)
            .chipSelect(0)
            .build();

    private final FourBySevenDisplay display;

    public SevenSegmentExample(DeviceRegistry registry) {
        display = new FourBySevenDisplay(
                registry.create(SPI_CONFIG),
                registry.create(PWM_CONFIG),
                registry.create(RESET_PIN_CONFIG).pin()
        );
    }

    @Override
    public void run() {
        display.reset();
        display.on();

        IntStream.range(0, 100000).forEach(display::write);

        display.off();
    }
}
