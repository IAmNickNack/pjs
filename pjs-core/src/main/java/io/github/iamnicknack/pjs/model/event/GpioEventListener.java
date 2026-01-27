package io.github.iamnicknack.pjs.model.event;

/**
 * The component can be notified of GPIO change events.
 */
@FunctionalInterface
public interface GpioEventListener<T extends GpioEventEmitter<T>> {

    /**
     * Callback for GPIO change events.
     * @param event the GPIO change event.
     */
    void onEvent(GpioChangeEvent<T> event);
}
