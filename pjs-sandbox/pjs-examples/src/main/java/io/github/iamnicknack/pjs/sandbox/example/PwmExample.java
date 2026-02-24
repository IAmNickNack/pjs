package io.github.iamnicknack.pjs.sandbox.example;

import io.github.iamnicknack.pjs.device.pwm.Pwm;
import io.github.iamnicknack.pjs.device.pwm.PwmConfig;
import io.github.iamnicknack.pjs.model.device.DeviceRegistry;
import io.github.iamnicknack.pjs.model.pin.Pin;
import io.github.iamnicknack.pjs.model.port.Port;
import org.jspecify.annotations.NonNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Stream;

public class PwmExample implements Runnable {

    private static final PwmConfig PWM_CONFIG = PwmConfig.builder()
            .chip(0)
            .channel(2)
            .frequency(220)
            .dutyRatio(0.5)
            .build();

    private final DeviceRegistry deviceRegistry;
    private final EasedRange frequencyRange = EasedRange.cosine(1, 440, 44);
    private final EasedRange dutyCycleRange = EasedRange.linear(0, 50, 20);


    public PwmExample(DeviceRegistry deviceRegistry) {
        this.deviceRegistry = deviceRegistry;
    }

    private void usePwmApi(Pwm pwm) {
        pwm.on();

        frequencyRange.forEach(frequency -> {
            pwm.setFrequency(frequency);
            sleep(100);
        });

        dutyCycleRange.forEach(dutyCycle -> {
            pwm.setDutyRatio((double)dutyCycle / 100.0);
            sleep(10);
        });
        dutyCycleRange.forEach(dutyCycle -> {
            pwm.setDutyRatio((double)dutyCycle / 100.0);
            sleep(50);
        });

        pwm.off();
    }

    private void usePortApi(Pwm pwm) {
        var portsPwm = new PortsPwm(pwm);

        portsPwm.enablePin.high();

        frequencyRange.forEach(frequency -> {
            portsPwm.frequencyPort.write(frequency);
            sleep(100);
        });

        dutyCycleRange.forEach(dutyCycle -> {
            portsPwm.dutyRatioPort.write((double)dutyCycle / 100.0);
            sleep(10);
        });
        dutyCycleRange.forEach(dutyCycle -> {
            portsPwm.dutyRatioPort.write((double)dutyCycle / 100.0);
            sleep(50);
        });

        portsPwm.enablePin.low();
    }

    @Override
    public void run() {
        var pwm = deviceRegistry.create(PWM_CONFIG);
//        usePwmApi(pwm);
        usePortApi(pwm);
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * Wrapper around a port which exposes all properties as individual ports.
     * @param dutyRatioPort the duty cycle port.
     * @param frequencyPort the frequency port.
     * @param enablePin the enable pin.
     */
    record PortsPwm(
            Port<Double> dutyRatioPort,
            Port<Integer> frequencyPort,
            Pin enablePin,
            Pin inversePolarityPin
    ) {
        public PortsPwm(Pwm delegate) {
            this(
                    Port.composite(delegate::getDutyRatio, delegate::setDutyRatio),
                    Port.composite(delegate::getFrequency, delegate::setFrequency),
                    Port.composite(delegate::isEnabled, delegate::setEnabled).pin(true, false),
                    Port.composite(delegate::getPolarity, delegate::setPolarity).pin(Pwm.Polarity.INVERTED, Pwm.Polarity.NORMAL)
            );
        }
    }

    /**
     * Range generator which implements easing functions.
     */
    private static class EasedRange implements Iterable<Integer> {

        private final int start;
        private final int end;
        private final int steps;
        private final DoubleUnaryOperator easing; // accepts t in [0,1] -> eased t in [0,1]

        public EasedRange(int start, int end, int steps, DoubleUnaryOperator easing) {
            if (steps <= 0) throw new IllegalArgumentException("steps must be > 0");
            this.start = start;
            this.end = end;
            this.steps = steps;
            this.easing = easing;
        }

        public static EasedRange cosine(int start, int end, int steps) {
            return new EasedRange(start, end, steps, t -> 0.5 * (1 - Math.cos(Math.PI * t)));
        }

        public static EasedRange linear(int start, int end, int steps) {
            return new EasedRange(start, end, steps, t -> t);
        }

        @Override
        public @NonNull Iterator<Integer> iterator() {
            return new Iterator<>() {
                private int idx = 0;

                @Override
                public boolean hasNext() {
                    return idx < steps;
                }

                @Override
                public Integer next() {
                    if (!hasNext()) throw new NoSuchElementException();
                    double t = (steps == 1) ? 1.0 : (double) idx / (steps - 1);
                    double eased = clamp(easing.applyAsDouble(t), 0.0, 1.0);
                    int value = (int) Math.round(start + eased * (end - start));
                    idx++;
                    return value;
                }
            };
        }

        public Stream<Integer> stream() {
            return Stream.iterate(start, i -> i < end, i -> i + 1)
                    .limit(steps)
                    .map(i -> (int) Math.round(start + easing.applyAsDouble((double) i / (steps - 1)) * (end - start)));
        }

        private static double clamp(double v, double lo, double hi) {
            return Math.max(lo, Math.min(v, hi));
        }
    }
}
