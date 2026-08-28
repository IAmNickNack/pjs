package io.github.iamnicknack.pjs.mock;

import io.github.iamnicknack.pjs.device.pwm.PwmBean;
import io.github.iamnicknack.pjs.device.pwm.PwmConfig;

public class MockPwmImpl extends PwmBean implements MockPwm {

    public MockPwmImpl(PwmConfig config) {
        super(config);
    }
}
