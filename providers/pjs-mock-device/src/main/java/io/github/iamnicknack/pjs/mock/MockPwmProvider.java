package io.github.iamnicknack.pjs.mock;

import io.github.iamnicknack.pjs.device.pwm.Pwm;
import io.github.iamnicknack.pjs.device.pwm.PwmConfig;
import io.github.iamnicknack.pjs.model.device.DeviceProvider;

public class MockPwmProvider implements DeviceProvider<Pwm, PwmConfig> {

    @Override
    public Pwm create(PwmConfig config) {
        return new MockPwm(config);
    }

    @Override
    public void close() {
        // do nothing
    }
}
