package io.github.iamnicknack.pjs.device.gpio;

import io.github.iamnicknack.pjs.model.device.Device;
import io.github.iamnicknack.pjs.model.event.GpioEventEmitter;
import io.github.iamnicknack.pjs.model.pin.BooleanPin;
import io.github.iamnicknack.pjs.model.pin.Pin;
import io.github.iamnicknack.pjs.model.port.Port;
import io.github.iamnicknack.pjs.util.GpioPinMask;

/**
 * A GPIO port maps a numeric value to one or many GPIO pins via the {@link Port} interface and can expose interrupt
 * events via the {@link GpioEventEmitter}.
 */
public interface GpioPort extends Port<Integer>, GpioEventEmitter<GpioPort>, Device<GpioPort> {

    /**
     * Returns a pin which when high sets all pins in the port to high.
     * @return the pin.
     */
    default Pin pin() {
        var config = (GpioPortConfig)getConfig();
        var allPinsMask = GpioPinMask.packBits(config.pinNumber());
        return new BooleanPin(this, i -> i != 0, i -> i ? allPinsMask : 0);
    }
}
