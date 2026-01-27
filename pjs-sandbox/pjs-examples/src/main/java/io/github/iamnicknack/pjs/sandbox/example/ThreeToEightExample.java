package io.github.iamnicknack.pjs.sandbox.example;

import io.github.iamnicknack.pjs.device.gpio.GpioPort;
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.device.gpio.GpioPortMode;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;
import io.github.iamnicknack.pjs.model.pin.Pin;
import io.github.iamnicknack.pjs.model.port.Port;

public class ThreeToEightExample implements Runnable {

    private final DeviceRegistry registry;

    public ThreeToEightExample(DeviceRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void run() {
        var device = new PortDevice(registry);
        device.on();

        try {
            for (int i = 0; i < 8; i++) {
                device.write(i);
                Thread.sleep(75);
            }
        } catch (InterruptedException e) {
            e.printStackTrace(System.err);
        }

        device.off();
    }

    static class PortDevice implements Port<Integer> {

        private static final GpioPortConfig PORT_CONFIG = GpioPortConfig.builder()
                .id("328-out")
                .pin(22, 23, 24)
                .mode(GpioPortMode.OUTPUT)
                .build();

        private static final GpioPortConfig ENABLE_CONFIG = GpioPortConfig.builder()
                .id("328-enable")
                .pin(25)
                .mode(GpioPortMode.OUTPUT)
                .build();

        private final GpioPort dataPort;
        private final Pin enablePin;

        public PortDevice(DeviceRegistry registry) {
            this.dataPort = registry.create(PORT_CONFIG);
            this.enablePin = registry.create(ENABLE_CONFIG).pin();
            enablePin.low();
            dataPort.write(0);
        }

        public void on() {
            enablePin.high();
        }

        public void off() {
            enablePin.low();
        }

        @Override
        public Integer read() {
            return dataPort.read();
        }

        @Override
        public void write(Integer value) {
            enablePin.low();
            dataPort.write(value);
            enablePin.high();
        }
    }

    static class PinsDevice implements Port<Integer> {

        private static final GpioPortConfig PIN0_CONFIG = GpioPortConfig.builder()
                .id("328-0")
                .pin(22)
                .mode(GpioPortMode.OUTPUT)
                .build();

        private static final GpioPortConfig PIN1_CONFIG = GpioPortConfig.builder()
                .id("328-1")
                .pin(23)
                .mode(GpioPortMode.OUTPUT)
                .build();

        private static final GpioPortConfig PIN2_CONFIG = GpioPortConfig.builder()
                .id("328-2")
                .pin(24)
                .mode(GpioPortMode.OUTPUT)
                .build();

        private static final GpioPortConfig ENABLE_CONFIG = GpioPortConfig.builder()
                .id("328-enable")
                .pin(25)
                .mode(GpioPortMode.OUTPUT)
                .build();

        private final Pin[] pins;
        private final Pin enablePin;

        public PinsDevice(DeviceRegistry registry) {
            pins = new Pin[] {
                    registry.create(PIN0_CONFIG).pin(),
                    registry.create(PIN1_CONFIG).pin(),
                    registry.create(PIN2_CONFIG).pin()
            };
            enablePin = registry.create(ENABLE_CONFIG).pin();
            enablePin.low();
        }

        public void on() {
            enablePin.high();
        }

        public void off() {
            enablePin.low();
        }

        @Override
        public Integer read() {
            return 0;
        }

        @Override
        public void write(Integer value) {
            enablePin.low();
            if ((value & 1) != 0) {
                pins[0].high();
            } else {
                pins[0].low();
            }

            if ((value & 2) != 0) {
                pins[1].high();
            } else {
                pins[1].low();
            }

            if ((value & 4) != 0) {
                pins[2].high();
            } else {
                pins[2].low();
            }
            enablePin.high();
        }
    }
}
