package io.github.iamnicknack.pjs.ffm.device.context;

import io.github.iamnicknack.pjs.ffm.context.method.MethodCaller;
import io.github.iamnicknack.pjs.ffm.context.method.MethodCallerFactory;

import java.lang.foreign.FunctionDescriptor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A factory for creating {@link MethodCaller} instances using a predefined lookup map.
 * <p>
 * This implementation allows for the creation of {@link MethodCaller} objects based on
 * a mapping of {@link Key} instances, where each {@link Key} is a combination of method
 * name and {@link FunctionDescriptor}.
 */
public class FakeMethodCallerFactory implements MethodCallerFactory {

    private final Map<Key, MethodCaller> lookup;
    private final Map<Key, Integer> invocationCounts;

    FakeMethodCallerFactory(Map<Key, MethodCaller> lookup) {
        this.lookup = lookup;
        this.invocationCounts = new HashMap<>();
    }

    public Map<Key, Integer> getInvocationCounts() {
        return Collections.unmodifiableMap(invocationCounts);
    }

    public Map<Key, MethodCaller> getLookup() {
        return Collections.unmodifiableMap(lookup);
    }

    public void assertInvoked() {
        assertThat(invocationCounts.size()).isEqualTo(lookup.size());
        invocationCounts.values().forEach(count -> assertThat(count).isGreaterThan(0));
    }

    @Override
    public MethodCaller create(String name, FunctionDescriptor functionDescriptor) {
        return args -> {
            var key = new Key(name, functionDescriptor);
            invocationCounts.put(key, invocationCounts.getOrDefault(key, 0) + 1);
            return lookup.get(key).call(args);
        };
    }

    public record Key(String name, FunctionDescriptor descriptor) {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<Key, MethodCaller> lookup = new HashMap<>();

        public Builder add(String name, FunctionDescriptor descriptor, MethodCaller caller) {
            lookup.put(new Key(name, descriptor), caller);
            return this;
        }

        public FakeMethodCallerFactory build() {
            return new FakeMethodCallerFactory(lookup);
        }
    }
}
