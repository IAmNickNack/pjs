package io.github.iamnicknack.pjs.model.event;

import io.github.iamnicknack.pjs.util.DirectExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;

/**
 * Basic implementation of {@link GpioEventEmitter} which can be used to manage listeners and forward events
 */
public class GpioEventEmitterDelegate<T extends GpioEventEmitter<T>>
        implements GpioEventEmitter<T>, GpioEventListener<T>, AutoCloseable {

    private final Logger logger = LoggerFactory.getLogger(GpioEventEmitterDelegate.class);
    private final Set<GpioEventListener<T>> listeners = new CopyOnWriteArraySet<>();

    /**
     * Executor used to invoke listeners within an event-forwarding operation.
     */
    private final ExecutorService listenerExecutor;

    public GpioEventEmitterDelegate() {
        this(new DirectExecutorService());
    }

    public GpioEventEmitterDelegate(ExecutorService listenerExecutor) {
        this.listenerExecutor = listenerExecutor;
    }

    public GpioEventEmitterDelegate(GpioEventEmitter<T> delegate) {
        this(delegate, new DirectExecutorService());
    }

    public GpioEventEmitterDelegate(GpioEventEmitter<T> delegate, ExecutorService listenerExecutor) {
        this.listenerExecutor = listenerExecutor;
        delegate.addListener(this);
    }

    public int getListenerCount() {
        return listeners.size();
    }

    @Override
    public void addListener(GpioEventListener<T> listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeListener(GpioEventListener<T> listener) {
        this.listeners.remove(listener);
    }

    @Override
    public void onEvent(GpioChangeEvent<T> event) {
        if (listeners.isEmpty()) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Forwarding event: {}", event);
        }
        listeners.forEach(l -> listenerExecutor.submit(() -> {
            try {
                l.onEvent(event);
            } catch (Exception ex) {
                logger.error("Failed to forward event: {}", event, ex);
            }
        }));
    }

    @Override
    public void close() throws Exception {
        listenerExecutor.shutdown();
        listenerExecutor.awaitTermination(1, java.util.concurrent.TimeUnit.SECONDS);
    }
}
