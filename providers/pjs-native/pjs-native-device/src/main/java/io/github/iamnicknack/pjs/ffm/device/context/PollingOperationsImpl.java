package io.github.iamnicknack.pjs.ffm.device.context;

import io.github.iamnicknack.pjs.ffm.context.method.MethodCaller;
import io.github.iamnicknack.pjs.ffm.context.NativeContext;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapper;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.Poll;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.ValueLayout;

public class PollingOperationsImpl implements PollingOperations {

    private final MethodCaller pollCaller;
    private final MemorySegmentMapper memorySegmentMapper;

    public PollingOperationsImpl(NativeContext nativeContext) {
        this(
                nativeContext.getCapturedStateMethodCallerFactory()
                        .create("poll", Descriptors.POLL),
                nativeContext.getMemorySegmentMapper()
        );
    }

    public PollingOperationsImpl(
            MethodCaller pollCaller,
            MemorySegmentMapper memorySegmentMapper
    ) {
        this.pollCaller = pollCaller;
        this.memorySegmentMapper = memorySegmentMapper;
    }

    @Override
    public Poll poll(Poll poll, int timeout) {
        var dataMemorySegment = memorySegmentMapper.segment(poll, Poll.class);
        this.pollCaller.call(dataMemorySegment, 1,  timeout);
        return memorySegmentMapper.value(dataMemorySegment, Poll.class);
    }

    static class Descriptors {
        static final FunctionDescriptor POLL = FunctionDescriptor.of(
                ValueLayout.JAVA_INT,   // return value
                ValueLayout.ADDRESS,    // pointer to poll data
                ValueLayout.JAVA_INT,   // poll data count
                ValueLayout.JAVA_INT    // timeout
        );
    }
}
