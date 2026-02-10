package io.github.iamnicknack.pjs.ffm.device;

import io.github.iamnicknack.pjs.device.gpio.GpioPort;
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.device.gpio.GpioPortProvider;
import io.github.iamnicknack.pjs.ffm.context.NativeContext;
import io.github.iamnicknack.pjs.ffm.device.context.*;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.*;
import io.github.iamnicknack.pjs.util.GpioPinMask;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * @see <a href="https://docs.kernel.org/userspace-api/gpio/gpio-v2-get-line-ioctl.html">gpio-v2-get-line-ioctl</a>
 * @see <a href="https://docs.kernel.org/userspace-api/gpio/gpio-v2-get-lineinfo-ioctl.html">gpio-v2-get-lineinfo-ioctl</a>
 */
public class NativePortProvider implements GpioPortProvider {

    private final Logger logger = LoggerFactory.getLogger(NativePortProvider.class);

    private final ChipInfo chipInfo;
    private final NativeContext nativeContext;
    private final FileOperations fileOperations;
    private final IoctlOperations ioctlOperations;

    public NativePortProvider(NativeContext context) {
        this(context, 0);
    }

    public NativePortProvider(NativeContext context, int chip) {
        this(
                new GpioOperationsImpl(context).chipInfo("/dev/gpiochip" + chip),
                context,
                new FileOperationsImpl(context),
                new IoctlOperationsImpl(context)
        );
    }

    public NativePortProvider(
            ChipInfo chipInfo,
            NativeContext nativeContext,
            FileOperations fileOperations,
            IoctlOperations ioctlOperations
    ) {
        this.chipInfo = chipInfo;
        this.nativeContext = nativeContext;
        this.fileOperations = fileOperations;
        this.ioctlOperations = ioctlOperations;
    }

    @Override
    public GpioPort create(GpioPortConfig config) {
        try(var fileDescriptor = fileOperations
                .openFd(chipInfo.getPath(), FileOperationsImpl.Flags.O_RDWR | FileOperationsImpl.Flags.O_CLOEXEC)) {
            Arrays.stream(config.pinNumber()).forEach(pinNumber -> {
                var lineInfoResult = ioctlOperations
                        .ioctl(fileDescriptor.fd(), GpioConstants.GPIO_V2_GET_LINEINFO_IOCTL, LineInfo.ofOffset(pinNumber));
                if (PinFlag.USED.isSet(lineInfoResult.flags())) {
                    throw new IllegalStateException("Pin " + lineInfoResult.offset() + " is already in use.");
                }
            });

            var lineRequest = createLineRequest(config);
            var lineRequestResult = ioctlOperations.ioctl(fileDescriptor.fd(), GpioConstants.GPIO_V2_GET_LINE_IOCTL, lineRequest);

            logger.debug("Created port {} with result {}", config.id(), lineRequestResult);

            var port = new NativePort(config, fileOperations.createFileDescriptor(lineRequestResult.fd()), nativeContext);
            if (config.defaultValue() >= 0) {
                port.write(config.defaultValue());
            }
            return port;
        }
    }

    private @NonNull LineRequest createLineRequest(GpioPortConfig config) {
        var eventFlags = switch (config.eventMode()) {
            case NONE -> 0;
            case RISING -> PinFlag.EDGE_RISING.value;
            case FALLING -> PinFlag.EDGE_FALLING.value;
            case BOTH -> PinFlag.EDGE_RISING.value | PinFlag.EDGE_FALLING.value;
        };

        var modeFlags = switch (config.portMode()) {
            case INPUT -> PinFlag.INPUT.value;
            case INPUT_PULLDOWN -> PinFlag.BIAS_PULL_DOWN.value | PinFlag.INPUT.value;
            case INPUT_PULLUP -> PinFlag.BIAS_PULL_UP.value | PinFlag.INPUT.value;
            case OUTPUT -> PinFlag.OUTPUT.value;
            case OUTPUT_OPENDRAIN -> PinFlag.OPEN_DRAIN.value | PinFlag.OUTPUT.value;
            case OUTPUT_OPENSOURCE -> PinFlag.OPEN_SOURCE.value | PinFlag.OUTPUT.value;
        };

        logger.debug("Creating line request for port {} with mode {} ({}), event mode {} ({}), debounce delay {}",
                config.id(),
                config.portMode(),
                Integer.toBinaryString(modeFlags),
                config.eventMode(),
                Integer.toBinaryString(eventFlags),
                config.debounceDelay()
        );

        var debounceAttr = new LineAttribute(LineAttribute.Id.DEBOUNCE_PERIOD_US, (eventFlags != 0) ? config.debounceDelay() : 0);
        var debounceConfig = new LineConfigAttribute(debounceAttr, GpioPinMask.gpioMaskFor(config.pinNumber()));
        var lineConfig = new LineConfig(modeFlags | eventFlags, new LineConfigAttribute[] { debounceConfig });
        return new LineRequest(config.pinNumber(), config.id(), lineConfig, 0, 0);
    }
}
