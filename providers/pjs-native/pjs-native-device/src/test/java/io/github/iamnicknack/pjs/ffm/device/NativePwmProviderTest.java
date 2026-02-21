package io.github.iamnicknack.pjs.ffm.device;

import io.github.iamnicknack.pjs.device.pwm.Pwm;
import io.github.iamnicknack.pjs.device.pwm.PwmConfig;
import io.github.iamnicknack.pjs.ffm.device.context.SysfsOperationsFactory;
import io.github.iamnicknack.pjs.ffm.device.context.SysfsOperationsImpl;
import io.github.iamnicknack.pjs.ffm.device.context.VirtualFileOperations;
import org.junit.jupiter.api.Test;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.List;

import static io.github.iamnicknack.pjs.ffm.device.NativePwmProviderTest.FakePwmSysfsOperations.createChipSysfsFilesystem;
import static org.assertj.core.api.Assertions.assertThat;

class NativePwmProviderTest {

    @Test
    void canCreatePwmDevice() {
        var fileOperations = new VirtualFileOperations();
        SysfsOperationsFactory factory = devicePath -> new FakePwmSysfsOperations(fileOperations, devicePath);
        createChipSysfsFilesystem(fileOperations, "/sys/class/pwm/pwmchip0", 1);

        try (var provider = new NativePwmProvider(factory);
             var device = provider.create(PwmConfig.builder().chip(0).channel(0).build())) {
            device.setFrequency(440);
            device.setDutyRatio(0.50);
            device.setPolarity(Pwm.Polarity.NORMAL);
            device.on();
            assertThat(device.read()).isTrue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sysfs operations for PWM testing purposes
     */
    static class FakePwmSysfsOperations extends SysfsOperationsImpl {

        private final VirtualFileOperations fileOperations;
        private final String devicePath;

        /**
         * Constructor
         * @param fileOperations virtual file operations
         * @param devicePath base path of the device
         */
        public FakePwmSysfsOperations(
                VirtualFileOperations fileOperations,
                String devicePath
        ) {
            super(fileOperations.root().resolve(devicePath), fileOperations);
            this.fileOperations = fileOperations;
            this.devicePath = devicePath;
        }

        /**
         * Overriding {@link SysfsOperationsImpl#writeInt(String, int)} to create pwm sysfs filesystem on first call
         * to {@code writeInt(CHIP_EXPORT_PATH, value)}
         *
         * {@inheritDoc}
         */
        @Override
        public void writeInt(String path, int value) {
            super.writeInt(path, value);
            if (path.equals(NativePwmProvider.CHIP_EXPORT_PATH)) {
                try {
                    createPwmSysfsFilesystem(
                            fileOperations,
                            fileOperations.root()
                                    .resolve(devicePath)
                                    .resolve(NativePwmProvider.PWM_PATH + value)
                                    .toString()
                    );
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        /**
         * Create sysfs filesystem for pwmchipN device
         * @param fileOperations virtual file operations
         * @param devicePath device path
         * @param numChannels number of pwm channels to make available
         */
        public static void createChipSysfsFilesystem(VirtualFileOperations fileOperations, String devicePath, int numChannels) {
            try {
                var root = fileOperations.root().resolve(devicePath);
                Files.createDirectories(root);
                for (var path : List.of(NativePwmProvider.CHIP_EXPORT_PATH, NativePwmProvider.CHIP_UNEXPORT_PATH, NativePwmProvider.CHIP_NPWM_PATH)) {
                    Files.createFile(root.resolve(path));
                }

                try (var out = new PrintWriter(new OutputStreamWriter(Files.newOutputStream(root.resolve(NativePwmProvider.CHIP_NPWM_PATH))))) {
                    out.println(numChannels);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Create sysfs filesystem for pwmN device
         * @param fileOperations virtual file operations
         * @param devicePath pwmN device path
         */
        public static void createPwmSysfsFilesystem(VirtualFileOperations fileOperations, String devicePath) {
            try {
                var root = fileOperations.root().resolve(devicePath);
                Files.createDirectories(root);
                for (var path : List.of(NativePwm.ENABLE_PATH, NativePwm.PERIOD_PATH, NativePwm.DUTY_CYCLE_PATH, NativePwm.POLARITY_PATH)) {
                    Files.createFile(root.resolve(path));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
