package io.github.iamnicknack.pjs.ffm.event;

import io.github.iamnicknack.pjs.model.event.GpioChangeEventType;

/**
 * Poll event data
 * @param eventType the type of event
 * @param timestamp the event timestamp
 * @param offset the offset which triggered the event
 */
public record PollEvent(
        PollEventType eventType,
        long timestamp,
        int offset
) {
    public GpioChangeEventType asLineChangeEventType() {
        return switch (this.eventType) {
            case RISING -> GpioChangeEventType.RISING;
            case FALLING -> GpioChangeEventType.FALLING;
            case ANY -> GpioChangeEventType.ANY;
            case NONE -> GpioChangeEventType.NONE;
        };
    }
}
