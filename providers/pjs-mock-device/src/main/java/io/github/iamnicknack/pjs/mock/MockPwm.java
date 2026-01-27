package io.github.iamnicknack.pjs.mock;

import io.github.iamnicknack.pjs.device.pwm.Pwm;
import io.github.iamnicknack.pjs.device.pwm.PwmBean;
import io.github.iamnicknack.pjs.device.pwm.PwmConfig;

public class MockPwm extends PwmBean implements Pwm {

    public MockPwm(PwmConfig config) {
        super(config);
    }
}
