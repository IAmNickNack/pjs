// java
package io.github.iamnicknack.pjs.util;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ExecutorService that runs tasks on the calling thread.
 */
public final class DirectExecutorService extends AbstractExecutorService {

    private final AtomicInteger running = new AtomicInteger();
    private volatile boolean shutdown;

    @Override
    public void execute(Runnable command) {
        if (shutdown) throw new RejectedExecutionException("Executor is shutdown");
        running.incrementAndGet();
        try {
            command.run();
        } finally {
            running.decrementAndGet();
        }
    }

    @Override
    public void shutdown() {
        shutdown = true;
    }

    @Override
    public List<Runnable> shutdownNow() {
        shutdown = true;
        return List.of();
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public boolean isTerminated() {
        return shutdown && running.get() == 0;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long deadline = System.nanoTime() + unit.toNanos(timeout);
        while (!isTerminated()) {
            if (System.nanoTime() >= deadline) return false;
            Thread.sleep(1);
        }
        return true;
    }
}
