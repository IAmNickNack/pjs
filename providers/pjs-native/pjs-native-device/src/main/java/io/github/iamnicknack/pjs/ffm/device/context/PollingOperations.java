package io.github.iamnicknack.pjs.ffm.device.context;

import io.github.iamnicknack.pjs.ffm.device.context.gpio.Poll;

/**
 * Native operations for event polling
 * <p>
 * The current specification states that only a single file descriptor can be passed and therefore requires
 * that multiple file descriptors require multiple calls to poll and consequently multiple threads.
 * </p>
 * <p>
 * An optimisation could be to allow multiple file descriptors per poll request and monitor all in the same
 * thread. TBD
 * </p>
 */
public interface PollingOperations {

    /**
     * Native call to `poll`. Implementations of this function will only ever pass a single file descriptor.
     * @param poll request data
     * @param timeout timeout in milliseconds
     * @return poll structure with `revents` set
     */
    Poll poll(Poll poll, int timeout);
}
