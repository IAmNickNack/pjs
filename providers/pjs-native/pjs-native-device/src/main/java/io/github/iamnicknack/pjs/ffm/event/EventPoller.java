package io.github.iamnicknack.pjs.ffm.event;

public interface EventPoller extends Runnable {
    void stop();

    boolean isRunning();

    @Override
    void run();

    EventPoller NOOP = new EventPoller() {
        @Override
        public void stop() {}

        @Override
        public boolean isRunning() { return false; }

        @Override
        public void run() {}
    };
}
