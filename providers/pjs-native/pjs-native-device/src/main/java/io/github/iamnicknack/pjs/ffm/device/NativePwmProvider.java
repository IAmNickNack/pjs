package io.github.iamnicknack.pjs.ffm.device;

import io.github.iamnicknack.pjs.device.pwm.Pwm;
import io.github.iamnicknack.pjs.device.pwm.PwmConfig;
import io.github.iamnicknack.pjs.device.pwm.PwmProvider;
import io.github.iamnicknack.pjs.ffm.device.context.SysfsOperationsFactory;

import static io.github.iamnicknack.pjs.ffm.device.NativePwm.ENABLE_PATH;

public class NativePwmProvider implements PwmProvider {

    static final String CHIP_PATH = "/sys/class/pwm/pwmchip";
    static final String CHIP_EXPORT_PATH = "export";
    static final String CHIP_UNEXPORT_PATH = "unexport";
    static final String CHIP_NPWM_PATH = "npwm";
    static final String PWM_PATH = "pwm";

    private final SysfsOperationsFactory sysfsOperationsFactory;

    public NativePwmProvider(SysfsOperationsFactory sysfsOperationsFactory) {
        this.sysfsOperationsFactory = sysfsOperationsFactory;
    }

    @Override
    public Pwm create(PwmConfig config) {
        var chip = sysfsOperationsFactory.createSysfsOperations(CHIP_PATH + config.chip());
        if (!chip.exists()) {
            throw new IllegalArgumentException("PWM chip " + config.chip() + " does not exist");
        }

        var channel = sysfsOperationsFactory.createSysfsOperations(chip.path().resolve(PWM_PATH + config.channel()));
        if (!channel.exists()) {
            var pwm = chip.readInt(CHIP_NPWM_PATH);
            if (config.channel() < 0 || config.channel() >= pwm) {
                throw new IllegalArgumentException("PWM channel " + config.channel() + " does not exist on chip " + chip + ".");
            }

            chip.writeInt(CHIP_EXPORT_PATH, config.channel());
            var timeout = 1000;
            while (!channel.exists() && timeout > 0) {
                // wait for channel to be exported
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                timeout--;
            }
            if (timeout == 0) {
                throw new IllegalStateException("Timeout waiting for PWM channel " + channel + " to be exported.");
            }
        } else {
            // disable channel if already enabled
            channel.writeInt(ENABLE_PATH, 0);
        }

        return new NativePwm(config, channel);
    }
}
