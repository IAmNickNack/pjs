package io.github.iamnicknack.pjs.pi4j;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.*;
import io.github.iamnicknack.pjs.device.gpio.GpioPort;
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.device.gpio.GpioPortMode;
import io.github.iamnicknack.pjs.device.gpio.GpioPortProvider;
import io.github.iamnicknack.pjs.model.device.DeviceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Pi4jPortProvider implements GpioPortProvider {

    private final Logger logger = LoggerFactory.getLogger(Pi4jPortProvider.class);

    private final Context pi4jContext;
    private final Map<String, List<String>> deviceIdMap = new HashMap<>();

    public Pi4jPortProvider(Context pi4jContext) {
        this.pi4jContext = pi4jContext;
    }

    @Override
    public GpioPort create(GpioPortConfig config) {
        if (config.portMode().isSet(GpioPortMode.OUTPUT)) {
            var devices = createOutputs(config);
            return new Pi4jOutputPort(
                    config,
                    devices,
                    () -> removeDevices(config)
            );
        } else {
            var devices = createInputs(config);
            return new Pi4jInputPort(
                    config,
                    devices,
                    () -> removeDevices(config)
            );
        }
    }

    private List<DigitalInput> createInputs(GpioPortConfig config) {
        var mode = switch (config.portMode()) {
            case INPUT_PULLDOWN -> PullResistance.PULL_DOWN;
            case INPUT_PULLUP -> PullResistance.PULL_UP;
            default -> PullResistance.OFF;
        };
        var devices = Arrays.stream(config.pinNumber())
                .mapToObj(pin -> DigitalInputConfig.newBuilder(pi4jContext)
                        .bcm(pin)
                        .pull(mode)
                        .debounce((long) config.debounceDelay())
                        .build()
                )
                .map(pi4jContext::create)
                .toList();

        deviceIdMap.put(config.id(), devices.stream().map(DigitalInput::getId).toList());

        return devices;
    }

    private List<DigitalOutput> createOutputs(GpioPortConfig config) {
        var devices = Arrays.stream(config.pinNumber())
                .mapToObj(pin -> DigitalOutputConfig.newBuilder(pi4jContext)
                        .bcm(pin)
                        .initial(config.defaultValue() > 0 ? DigitalState.HIGH : DigitalState.LOW)
                        .build()
                )
                .map(pi4jContext::create)
                .toList();

        deviceIdMap.put(config.id(), devices.stream().map(DigitalOutput::getId).toList());

        return devices;
    }

    private void removeDevices(DeviceConfig<?> config) {
        var deviceCount = deviceIdMap.getOrDefault(config.getId(), List.of()).size();
        logger.info("Removing {} Pi4J component devices of: {}", deviceCount, config.getId());

        deviceIdMap.getOrDefault(config.getId(), List.of())
                .forEach(id -> {
                    logger.info("Removing Pi4J device: {}, component of: {}", id, config.getId());
                    pi4jContext.registry().remove(id);
                });

        deviceIdMap.remove(config.getId());
    }
}
