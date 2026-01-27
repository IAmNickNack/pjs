import io.github.iamnicknack.pjs.model.device.DeviceRegistryLoader;

module pjs.core {
    exports io.github.iamnicknack.pjs.device.gpio;
    exports io.github.iamnicknack.pjs.device.i2c;
    exports io.github.iamnicknack.pjs.device.i2c.impl;
    exports io.github.iamnicknack.pjs.device.pwm;
    exports io.github.iamnicknack.pjs.device.spi;
    exports io.github.iamnicknack.pjs.device.spi.impl;
    exports io.github.iamnicknack.pjs.logging;
    exports io.github.iamnicknack.pjs.model;
    exports io.github.iamnicknack.pjs.model.device;
    exports io.github.iamnicknack.pjs.model.event;
    exports io.github.iamnicknack.pjs.model.pin;
    exports io.github.iamnicknack.pjs.model.port;
    exports io.github.iamnicknack.pjs.impl;

    requires org.jspecify;
    requires org.slf4j;
    requires pjs.util;

    uses DeviceRegistryLoader;
}