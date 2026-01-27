package io.github.iamnicknack.pjs.ffm.event;

import java.util.List;

/**
 * Handler for detected poll events
 */
@FunctionalInterface
public interface PollEventsCallback {

    /**
     * Notification for poll events
     * @param pollEvents the detected events
     */
    void callback(EventPoller poller, List<PollEvent> pollEvents);
}
