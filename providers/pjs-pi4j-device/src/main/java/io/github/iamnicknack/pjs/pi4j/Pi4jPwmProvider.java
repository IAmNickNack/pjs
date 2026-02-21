package io.github.iamnicknack.pjs.pi4j;

import com.pi4j.context.Context;
import com.pi4j.io.pwm.PwmConfigBuilder;
import io.github.iamnicknack.pjs.device.pwm.Pwm;
import io.github.iamnicknack.pjs.device.pwm.PwmConfig;
import io.github.iamnicknack.pjs.device.pwm.PwmProvider;

import java.util.HashMap;
import java.util.Map;

public class Pi4jPwmProvider implements PwmProvider {

    private final Context pi4jContext;
    private final Map<String, String> deviceIdMap = new HashMap<>();

    public Pi4jPwmProvider(Context pi4jContext) {
        this.pi4jContext = pi4jContext;
    }

    @Override
    public Pwm create(PwmConfig config) {
        com.pi4j.io.pwm.PwmConfig pi4jPwmConfig = PwmConfigBuilder.newInstance(pi4jContext)
                .chip(config.chip())
                .channel(config.channel())
                .frequency(config.frequency())
                .dutyCycle(config.dutyCyclePercent())
                .build();

        com.pi4j.io.pwm.Pwm pi4jPwm = pi4jContext.create(pi4jPwmConfig);
        deviceIdMap.put(config.getId(), pi4jPwm.getId());

        return new Pi4jPwm(
                config,
                pi4jPwm,
                () -> pi4jContext.registry().remove(deviceIdMap.get(config.getId()))
        );
    }
}
