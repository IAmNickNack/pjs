package io.github.iamnicknack.pjs.ffm.event.debounce;

import io.github.iamnicknack.pjs.ffm.event.EventPoller;
import io.github.iamnicknack.pjs.ffm.event.PollEvent;
import io.github.iamnicknack.pjs.ffm.event.PollEventsCallback;

import java.util.List;

/**
 * Callback which propagates events without any debounce filtering.
 */
public class HardwareDebounceCallback implements PollEventsCallback {
    private final PollEventsCallback delegate;

    public HardwareDebounceCallback(PollEventsCallback delegate) {
        this.delegate = delegate;
    }

    @Override
    public void callback(EventPoller poller, List<PollEvent> pollEvents) {
        delegate.callback(poller, pollEvents);
    }
}
