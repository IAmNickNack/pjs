package io.github.iamnicknack.pjs.device.pwm;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PwmBeanTest {

    @Test
    void canSetFrequency() {
        var config = PwmConfig.builder()
                .frequency(1000)
                .build();

        var pwm = new PwmBean(config);
        assertThat(pwm.getFrequency()).isEqualTo(1000);
        assertThat(pwm.getPeriod()).isEqualTo(1_000_000_000 / 1000);
    }

    @Test
    void canSetPeriod() {
        var config = PwmConfig.builder()
                .period(1_000_000)
                .build();

        var pwm = new PwmBean(config);
        assertThat(pwm.getPeriod()).isEqualTo(1_000_000);
        assertThat(pwm.getFrequency()).isEqualTo(1000);
    }

    @Test
    void canSetDutyCycle() {
        var config = PwmConfig.builder()
                .dutyCycle(500_000)
                .period(1_000_000)
                .build();

        var pwm = new PwmBean(config);
        assertThat(pwm.getDutyCycle()).isEqualTo(500_000);
        assertThat(pwm.getDutyRatio()).isEqualTo(0.5);
    }

    @Test
    void canSetDutyRatio() {
        var config = PwmConfig.builder()
                .dutyRatio(0.5)
                .period(1_000_000)
                .build();

        var pwm = new PwmBean(config);
        assertThat(pwm.getDutyRatio()).isEqualTo(0.5);
        assertThat(pwm.getDutyCycle()).isEqualTo(500_000);
    }
}
