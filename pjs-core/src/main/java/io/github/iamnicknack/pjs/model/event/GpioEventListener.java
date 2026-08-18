package io.github.iamnicknack.pjs.model.event;

/**
 * The component can be notified of GPIO change events.
 * @param <T> the type of the GPIO event emitter being listened to
 */
@FunctionalInterface
public interface GpioEventListener<T extends GpioEventEmitter<T>> {

    /**
     * Callback for GPIO change events.
     * @param event the GPIO change event.
     */
    void onEvent(GpioChangeEvent<T> event);
}
