package io.github.iamnicknack.pjs.model.event;

/**
 * Contract for devices which can emit GPIO events.
 */
public interface GpioEventEmitter<T extends GpioEventEmitter<T>> {

    /**
     * Subscribe to events from this device.
     * @param listener the callback to subscribe.
     */
    void addListener(GpioEventListener<T> listener);

    /**
     * Unsubscribe from events from this device.
     * @param listener the callback to unsubscribe.
     */
    void removeListener(GpioEventListener<T> listener);
}
