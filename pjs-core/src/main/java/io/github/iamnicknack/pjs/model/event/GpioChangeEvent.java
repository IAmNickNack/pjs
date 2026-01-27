package io.github.iamnicknack.pjs.model.event;

/**
 * An event triggered when a GPIO pin changes state.
 * @param port the GPIO port that changed state
 * @param eventType the type of event that triggered this change
 */
public record GpioChangeEvent<T extends GpioEventEmitter<T>>(
        T port,
        GpioChangeEventType eventType
) {
}
