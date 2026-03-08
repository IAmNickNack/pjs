package io.github.iamnicknack.pjs.ffm.device.context;

import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapper;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapperImpl;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.Poll;
import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;

class PollingOperationsImplTest {

    private final MemorySegmentMapper mapper = new MemorySegmentMapperImpl(Arena.ofAuto());

    @Test
    void test() {
        var poll = new Poll(1, 2, 0);
        performTest(
                builder -> builder
                        .addMethodCaller("poll", PollingOperationsImpl.Descriptors.POLL, args -> {
                            var segmentPoll = mapper.value((MemorySegment) args[0], Poll.class);
                            assertThat(segmentPoll).isEqualTo(poll);
                            var pollResult = new Poll(segmentPoll.fd(), segmentPoll.events(), 3);
                            ((MemorySegment)args[0]).copyFrom(mapper.segment(pollResult));
                            return 0;
                        }),
                pollingOperations -> {
                    var result = pollingOperations.poll(poll, 1);
                    assertThat(result.revents()).isEqualTo(3);
                }
        );
    }

    private void performTest(
            UnaryOperator<FakeNativeContext.Builder> configurer,
            Consumer<PollingOperations> verifier
    ) {
        var context = configurer.apply(FakeNativeContext.builder()).build();
        var fileOperations = new PollingOperationsImpl(context);
        verifier.accept(fileOperations);

        var methodCallerFactory = (FakeMethodCallerFactory)context.getMethodCallerFactory();
        methodCallerFactory.assertInvoked();
    }
}