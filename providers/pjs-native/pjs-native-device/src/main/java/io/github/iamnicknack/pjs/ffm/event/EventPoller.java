package io.github.iamnicknack.pjs.ffm.event;

import io.github.iamnicknack.pjs.ffm.device.context.FileDescriptor;

/**
 * {@link Runnable} responsible for polling GPIO events
 */
public interface EventPoller extends Runnable {
    /**
     * Start polling GPIO events.
     */
    void start();

    /**
     * Stop polling GPIO events.
     */
    void stop();

    /**
     * Check if the event poller is currently running.
     * @return true if running, false otherwise
     */
    boolean isRunning();

    /**
     * Polling operation
     */
    @Override
    void run();

    /**
     * Factory for creating event pollers
     */
    interface Factory extends AutoCloseable {
        /**
         * Create a new event poller.
         * @param fileDescriptor the file descriptor to poll events from
         * @param pollEventsCallback the callback to invoke when events are detected
         * @return a new event poller
         */
        EventPoller create(FileDescriptor fileDescriptor, PollEventsCallback pollEventsCallback);

        /**
         * Close the factory and release any resources.
         */
        @Override
        default void close() {}
    }

    /**
     * No-op event poller.
     */
    EventPoller NOOP = new EventPoller() {
        public void start() {}

        @Override
        public void stop() {}

        @Override
        public boolean isRunning() { return false; }

        @Override
        public void run() {}
    };

    /**
     * No-op factory.
     */
    Factory NOOP_FACTORY = (_, _) -> NOOP;
}
