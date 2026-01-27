package io.github.iamnicknack.pjs.ffm.event;

/**
 * Recognised GPIO event types
 */
public enum PollEventType {
    NONE(0),
    RISING(1),
    FALLING(2),
    ANY(3);

    public final int value;

    PollEventType(int value) {
        this.value = value;
    }

    static PollEventType from(int value) {
        return switch (value) {
            case 1 -> RISING;
            case 2 -> FALLING;
            case 3 -> ANY;
            default -> NONE;
        };
    }
}
