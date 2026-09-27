package io.github.iamnicknack.pjs.mock;

import io.github.iamnicknack.pjs.device.pwm.PwmBean;
import io.github.iamnicknack.pjs.device.pwm.PwmConfig;

public class MockPwmImpl extends PwmBean implements MockPwm {

    private boolean isClosed = false;

    public MockPwmImpl(PwmConfig config) {
        super(config);
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public void close() throws Exception {
        isClosed = true;
        super.close();
    }
}
