package io.github.iamnicknack.pjs.ffm.device;

import io.github.iamnicknack.pjs.device.gpio.GpioEventMode;
import io.github.iamnicknack.pjs.device.gpio.GpioPort;
import io.github.iamnicknack.pjs.device.gpio.GpioPortConfig;
import io.github.iamnicknack.pjs.device.gpio.GpioPortMode;
import io.github.iamnicknack.pjs.device.gpio.GpioPortProvider;
import io.github.iamnicknack.pjs.ffm.context.NativeContext;
import io.github.iamnicknack.pjs.ffm.device.context.FileDescriptor;
import io.github.iamnicknack.pjs.ffm.device.context.FileOperations;
import io.github.iamnicknack.pjs.ffm.device.context.FileOperationsImpl;
import io.github.iamnicknack.pjs.ffm.device.context.IoctlOperations;
import io.github.iamnicknack.pjs.ffm.device.context.PollingOperations;
import io.github.iamnicknack.pjs.ffm.device.context.PollingOperationsImpl;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.ChipInfo;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.GpioConstants;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.LineAttribute;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.LineConfig;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.LineConfigAttribute;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.LineInfo;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.LineRequest;
import io.github.iamnicknack.pjs.ffm.device.context.gpio.PinFlag;
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

    /**
     * System property to enable software debounce.
     * @see NativePort
     */
    public static final String SOFTWARE_DEBOUNCE_PROPERTY = "pjs.gpio.debounce.software";

    private final Logger logger = LoggerFactory.getLogger(NativePortProvider.class);

    private final ChipInfo chipInfo;
    private final NativeContext nativeContext;
    private final FileOperations fileOperations;
    private final IoctlOperations ioctlOperations;
    private final PollingOperations pollingOperations;

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
        this.pollingOperations = new PollingOperationsImpl(nativeContext);
    }

    @Override
    public GpioPort create(GpioPortConfig config) {
        try(var fileDescriptor = fileOperations.openFd(
                chipInfo.getPath(),
                FileOperationsImpl.Flags.O_RDWR | FileOperationsImpl.Flags.O_CLOEXEC)
        ) {
            checkLines(config, fileDescriptor);

            var lineRequest = createLineRequest(config);
            var lineRequestResult = ioctlOperations.ioctl(
                    fileDescriptor,
                    GpioConstants.GPIO_V2_GET_LINE_IOCTL,
                    lineRequest
            );

            logger.debug("Created port {} with result {}", config.id(), lineRequestResult);

            var port = ((config.eventMode() != GpioEventMode.NONE) && (config.portMode().isSet(GpioPortMode.INPUT))
                    ? new NativePort(
                            config,
                            fileOperations.createFileDescriptor(lineRequestResult.fd()),
                            ioctlOperations,
                            fileOperations,
                            pollingOperations
                    )
                    : new NativePort(
                            config,
                            fileOperations.createFileDescriptor(lineRequestResult.fd()),
                            ioctlOperations
                    )
            );

            if (config.defaultValue() >= 0) {
                port.write(config.defaultValue());
            }

            return port;
        }
    }

    /**
     * Check lines are available or not currently in use
     * @param config the requested config
     * @param fileDescriptor the file descriptor for the GPIO port
     */
    private void checkLines(GpioPortConfig config, FileDescriptor fileDescriptor) {
        Arrays.stream(config.pinNumber()).forEach(pinNumber -> {
            var lineInfoResult = ioctlOperations.ioctl(
                    fileDescriptor,
                    GpioConstants.GPIO_V2_GET_LINEINFO_IOCTL,
                    LineInfo.ofOffset(pinNumber)
            );
            if (PinFlag.USED.isSet(lineInfoResult.flags())) {
                throw new IllegalStateException("Pin " + lineInfoResult.offset() + " is already in use.");
            }
        });

    }

    /**
     * Construct a {@link LineRequest} from the given {@link GpioPortConfig}.
     * @param config the requested config
     * @return the constructed {@link LineRequest}
     */
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

        LineConfigAttribute[] attributes;
        if (config.portMode().isSet(GpioPortMode.INPUT) && eventFlags != 0 && !isSoftwareDebounceEnabled()) {
            logger.debug("Enabling hardware debounce filter for port {}", config.id());
            var debounceAttr = new LineAttribute(LineAttribute.Id.DEBOUNCE_PERIOD_US, config.debounceDelay());
            var mask = GpioPinMask.packBits(config.pinNumber());
            var debounceConfig = new LineConfigAttribute(debounceAttr, mask);
            attributes = new LineConfigAttribute[] { debounceConfig };
        } else {
            attributes = new LineConfigAttribute[0];
        }

        var lineConfig = new LineConfig(modeFlags | eventFlags, attributes);
        return new LineRequest(config.pinNumber(), config.id(), lineConfig, 0, 0);
    }

    /**
     * Check if software debounce is enabled.
     * <p>
     * Software debounce is enabled by default, but can be disabled by setting the system property
     * `pjs.gpio.debounce.software` to `false`. This is kind of hacky.
     * <ul>
     *     <li>Maybe this toggle should be part of the {@link GpioPortConfig}
     *     <li>Which properties should/could be `system properties` and which ought to
     *             be configurable per device, by the user requires some consideration.
     * </ul>
     * @return true if enabled, false otherwise
     */
    static boolean isSoftwareDebounceEnabled() {
        return System.getProperty(SOFTWARE_DEBOUNCE_PROPERTY, "true")
                .equalsIgnoreCase("true");
    }
}
