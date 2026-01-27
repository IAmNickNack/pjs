package io.github.iamnicknack.pjs.ffm.device;

import io.github.iamnicknack.pjs.ffm.context.NativeContext;
import io.github.iamnicknack.pjs.ffm.device.context.FileOperationsImpl;
import io.github.iamnicknack.pjs.ffm.device.context.SysfsOperationsImpl;
import io.github.iamnicknack.pjs.device.pwm.Pwm;
import io.github.iamnicknack.pjs.device.pwm.PwmConfig;
import io.github.iamnicknack.pjs.device.pwm.PwmProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

import static io.github.iamnicknack.pjs.ffm.device.NativePwm.ENABLE_PATH;

public class NativePwmProvider implements PwmProvider {


    private static final String CHIP_PATH = "/sys/class/pwm/pwmchip";
    private static final String CHIP_EXPORT_PATH = "export";
    private static final String CHIP_UNEXPORT_PATH = "unexport";
    private static final String CHIP_NPWM_PATH = "npwm";
    private static final String PWM_PATH = "pwm";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final NativeContext nativeContext;

    public NativePwmProvider(NativeContext context) {
        this.nativeContext = context;
    }

    @Override
    public Pwm create(PwmConfig config) {
        var fs = new FileOperationsImpl(nativeContext);

        var chip = new SysfsOperationsImpl(Path.of(CHIP_PATH + config.chip()), fs);
        if (!chip.exists()) {
            throw new IllegalArgumentException("PWM chip " + config.chip() + " does not exist");
        }

        var channel = new SysfsOperationsImpl(chip.path().resolve(PWM_PATH + config.channel()), fs);
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
