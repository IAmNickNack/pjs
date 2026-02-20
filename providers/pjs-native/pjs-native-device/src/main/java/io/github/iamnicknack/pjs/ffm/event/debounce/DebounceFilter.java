package io.github.iamnicknack.pjs.ffm.event.debounce;

import io.github.iamnicknack.pjs.ffm.event.PollEvent;

import java.util.function.Predicate;

/**
 * Stateful predicate to filter events based on a provided debounce period
 */
public class DebounceFilter implements Predicate<PollEvent> {
    private long last = 0;
    private final long debounce;

    public DebounceFilter(long debounce) {
        this.debounce = debounce;
    }

    @Override
    public boolean test(PollEvent pollEvent) {
        if ((pollEvent.timestamp() - last > debounce) || (last == 0)) {
            last = pollEvent.timestamp();
            return true;
        }
        return false;
    }
}
