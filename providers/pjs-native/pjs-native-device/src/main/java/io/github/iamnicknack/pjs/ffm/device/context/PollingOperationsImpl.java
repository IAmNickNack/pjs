package io.github.iamnicknack.pjs.ffm.device.context;

import io.github.iamnicknack.pjs.ffm.context.NativeContext;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.Poll;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.ValueLayout;

public class PollingOperationsImpl implements PollingOperations {

    private final NativeContext  nativeContext;
    private final NativeContext.MethodCaller poll;

    public PollingOperationsImpl(NativeContext nativeContext) {
        this.nativeContext = nativeContext;
        this.poll = nativeContext.capturedStateMethodCaller("poll", Descriptors.POLL);
    }

    @Override
    public Poll poll(Poll poll, int timeout) {
        var dataMemorySegment = nativeContext.segment(poll, Poll.class);
        this.poll.call(dataMemorySegment, 1,  timeout);
        return nativeContext.convertValue(dataMemorySegment, Poll.class);
    }

    private static class Descriptors {
        static final FunctionDescriptor POLL = FunctionDescriptor.of(
                ValueLayout.JAVA_INT,   // return value
                ValueLayout.ADDRESS,    // pointer to poll data
                ValueLayout.JAVA_INT,   // poll data count
                ValueLayout.JAVA_INT    // timeout
        );
    }
}
