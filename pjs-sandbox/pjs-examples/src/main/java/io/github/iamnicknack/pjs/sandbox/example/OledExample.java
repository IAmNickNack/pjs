package io.github.iamnicknack.pjs.sandbox.example;

import io.github.iamnicknack.pjs.device.i2c.I2CConfig;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.DisplayBuffer;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.Sh1106Driver;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.Sh1106Operations;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.TextOperations;

public class OledExample implements Runnable {

    private static final I2CConfig I2C_CONFIG = I2CConfig.builder()
            .bus(1)
            .build();

    private final Sh1106Operations deviceOperations;

    public OledExample(DeviceRegistry registry) {
        var i2c = registry.create(I2C_CONFIG);
        deviceOperations = new Sh1106Operations(new Sh1106Driver(i2c, 0x3c));
    }

    @Override
    public void run() {

        deviceOperations.init();
        deviceOperations.clear();
        deviceOperations.displayOn();
//        deviceOperations.setPosition(0, 0);
//        deviceOperations.drawText("Hello World!");

        var displayOperations = new DisplayBuffer();
        var textOperations = TextOperations.create(displayOperations);
        textOperations.drawText(4, 0, "012345678901234567890");

        displayOperations.copyTo(deviceOperations);
    }
}
