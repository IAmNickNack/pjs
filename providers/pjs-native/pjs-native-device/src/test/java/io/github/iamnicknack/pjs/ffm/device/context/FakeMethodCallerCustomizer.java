package io.github.iamnicknack.pjs.ffm.device.context;

import io.github.iamnicknack.pjs.ffm.context.method.MethodCaller;
import io.github.iamnicknack.pjs.ffm.context.method.MethodCallerCustomizer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class FakeMethodCallerCustomizer implements MethodCallerCustomizer {

    private final Map<String, MethodCaller> lookup;
    private final Map<String, Integer> invocationCounts;

    public FakeMethodCallerCustomizer(Map<String, MethodCaller> lookup) {
        this.lookup = lookup;
        this.invocationCounts = new HashMap<>();
    }

    public Map<String, Integer> getInvocationCounts() {
        return Collections.unmodifiableMap(invocationCounts);
    }

    public Map<String, MethodCaller> getLookup() {
        return Collections.unmodifiableMap(lookup);
    }

    public void assertInvoked() {
        assertThat(invocationCounts.size()).isEqualTo(lookup.size());
        invocationCounts.values().forEach(count -> assertThat(count).isGreaterThan(0));
    }

    @Override
    public MethodCaller customize(String name, MethodCaller methodCaller) {
        var caller = lookup.get(name);
        return (caller != null)
                ? args -> {
                    invocationCounts.put(name, invocationCounts.getOrDefault(name, 0) + 1);
                    return caller.call(args);
                }
                : methodCaller;
    }
}
