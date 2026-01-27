import io.github.iamnicknack.pjs.model.device.DeviceRegistryLoader;
import io.github.iamnicknack.pjs.pi4j.Pi4jDeviceRegistryLoader;

module pjs.pi4j {
    requires pjs.core;
    requires com.pi4j;
    requires org.slf4j;
    requires org.jspecify;

    exports io.github.iamnicknack.pjs.pi4j;

    provides DeviceRegistryLoader with Pi4jDeviceRegistryLoader;
}