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
     * Top-level executor used to forward events.
     */
    private final ExecutorService eventExecutor = new DirectExecutorService();
    /**
     * Executor used to invoke listeners within an event-forwarding operation.
     */
    private final ExecutorService listenerExecutor = eventExecutor;

//    private final ExecutorService eventExecutor = Executors.newSingleThreadExecutor();
//    private final ExecutorService listenerExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public GpioEventEmitterDelegate() {
        // do nothing
    }

    public GpioEventEmitterDelegate(GpioEventEmitter<T> delegate) {
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

        eventExecutor.submit(() -> {
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
        });
    }

    @Override
    public void close() throws Exception {
        eventExecutor.shutdown();
        listenerExecutor.shutdown();
        eventExecutor.awaitTermination(1, java.util.concurrent.TimeUnit.SECONDS);
        listenerExecutor.awaitTermination(1, java.util.concurrent.TimeUnit.SECONDS);
    }
}
