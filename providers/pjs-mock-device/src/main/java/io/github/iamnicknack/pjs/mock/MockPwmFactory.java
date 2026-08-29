package io.github.iamnicknack.pjs.mock;

import io.github.iamnicknack.pjs.device.pwm.Pwm;
import io.github.iamnicknack.pjs.device.pwm.PwmConfig;
import io.github.iamnicknack.pjs.model.device.DeviceFactory;

public class MockPwmFactory implements DeviceFactory<Pwm, PwmConfig> {

    @Override
    public Pwm create(PwmConfig config) {
        return new MockPwmImpl(config);
    }
}
