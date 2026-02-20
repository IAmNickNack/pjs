package io.github.iamnicknack.pjs.ffm.event;

/**
 * Debounce strategies
 */
public enum DebounceStrategy {
    /**
     * Leading-edge debounce. Triggers on the first change within a debounce window
     */
    SOFTWARE_LEADING_EDGE,
    /**
     * Trailing-edge debounce. Triggers on the last change within a debounce window
     */
    SOFTWARE_TRAILING_EDGE,
    /**
     * Hardware debounce. No-op strategy which delegates to the underlying hardware
     */
    HARDWARE;

    public static final String PROPERTY_KEY = "pjs.gpio.debounce-strategy";

    public static final DebounceStrategy DEFAULT = SOFTWARE_LEADING_EDGE;

    /**
     * Derive the system debounce strategy from system properties
     */
    public static DebounceStrategy fromProperty() {
        return valueOf(System.getProperty(PROPERTY_KEY, DEFAULT.name()).toUpperCase());
    }
}
