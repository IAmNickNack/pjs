package io.github.iamnicknack.pjs.sandbox.device.mcp;

import io.github.iamnicknack.pjs.model.event.GpioChangeEvent;
import io.github.iamnicknack.pjs.model.event.GpioChangeEventType;
import io.github.iamnicknack.pjs.model.event.GpioEventEmitter;
import io.github.iamnicknack.pjs.model.event.GpioEventEmitterDelegate;
import io.github.iamnicknack.pjs.model.event.GpioEventListener;
import io.github.iamnicknack.pjs.model.port.Port;
import io.github.iamnicknack.pjs.model.port.SerialPort;
import io.github.iamnicknack.pjs.sandbox.device.mcp.register.InMemoryRegister;
import io.github.iamnicknack.pjs.sandbox.device.mcp.register.Mcp23xxxRegisterFactory;
import io.github.iamnicknack.pjs.sandbox.device.mcp.register.WriteThroughRegister;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public final class Mcp23x08 implements GpioEventEmitter<Mcp23x08>, Port<Mcp23x08.State>, AutoCloseable {
    public static final int IODIR = 0x00;
    public static final int IPOL = 0x01;
    public static final int GPINTEN = 0x02;
    public static final int DEFVAL = 0x03;
    public static final int INTCON = 0x04;
    public static final int IOCON = 0x05;
    public static final int GPPU = 0x06;
    public static final int INTF = 0x07;
    public static final int INTCAP = 0x08;
    public static final int GPIO = 0x09;
    public static final int OLAT = 0x0A;

    public final SerialPort iodir;
    public final SerialPort ipol;
    public final SerialPort gpinten;
    public final SerialPort defval;
    public final SerialPort intcon;
    public final SerialPort iocon;
    public final SerialPort gppu;
    public final SerialPort intf;
    public final SerialPort intcap;
    public final SerialPort gpio;
    public final SerialPort olat;

    /**
     * Serial port used to read and write all ports when the device does not have the SEQOP bit set in IOCON.
     */
    public final SerialPort allPorts;
    /**
     * Reference to the cached IOCON register.
     */
    private final SerialPort cachedIocon;

    @Nullable
    private final GpioEventEmitter<?> interruptEventEmitter;
    private final GpioEventEmitterDelegate<Mcp23x08> eventEmitterDelegate = new GpioEventEmitterDelegate<>();

    /**
     * Enum representing the control modes available in the IOCON register.
     */
    public enum IoControlMode {
        SEQOP(1 << 5),
        DISSLW(1 << 4),
        HAEN(1 << 3),
        ODR(1 << 2),
        INTPOL(1 << 1);

        public final int value;

        IoControlMode(int value) {
            this.value = value;
        }

        public boolean isSet(int value) {
            return (value & this.value) != 0;
        }
    }

    /**
     * Constructor with no interrupt support.
     * @param registerFactory the factory for creating the register ports.
     */
    public Mcp23x08(Mcp23xxxRegisterFactory registerFactory) {
        this(registerFactory, null);
    }

    /**
     * Constructor with interrupt support.
     * @param registerFactory the factory for creating the register ports.
     * @param interruptEventEmitter the event emitter for interrupt events.
     */
    public Mcp23x08(
            Mcp23xxxRegisterFactory registerFactory,
            @Nullable
            GpioEventEmitter<?> interruptEventEmitter
    ) {
        var cacheRegisterFactory = new WriteThroughRegister.Factory(registerFactory, new InMemoryRegister.Factory(new byte[11]));

        this.iodir = cacheRegisterFactory.register(IODIR);
        this.ipol = cacheRegisterFactory.register(IPOL);
        this.gpinten = cacheRegisterFactory.register(GPINTEN);
        this.defval = cacheRegisterFactory.register(DEFVAL);
        this.intcon = cacheRegisterFactory.register(INTCON);
        this.iocon = cacheRegisterFactory.register(IOCON);
        this.gppu = cacheRegisterFactory.register(GPPU);
        this.intf = cacheRegisterFactory.register(INTF);
        this.intcap = cacheRegisterFactory.register(INTCAP).input();
        this.gpio = cacheRegisterFactory.register(GPIO);
        this.olat = cacheRegisterFactory.register(OLAT);

        this.allPorts = this.iodir;
        this.cachedIocon = ((WriteThroughRegister) this.iocon).getCache();

        this.interruptEventEmitter = interruptEventEmitter;
    }

    /**
     * Read the state of all ports. This is only supported when the SEQOP disable-bit is not set in IOCON.
     * @return the state of all ports.
     * @throws IllegalStateException if SEQOP is disabled.
     */
    @Override
    public State read() {
        if (IoControlMode.SEQOP.isSet(cachedIocon.read())) {
            throw new IllegalStateException("Cannot read all ports when SEQOP is disabled.");
        }
        return new State(allPorts.readBytes(11));
    }

    /**
     * Write the state of all ports. This is only supported when the SEQOP disable-bit is not set in IOCON.
     * @param value the state to write.
     * @throws IllegalStateException if SEQOP is disabled.
     */
    @Override
    public void write(State value) {
        if (IoControlMode.SEQOP.isSet(cachedIocon.read())) {
            throw new IllegalStateException("Cannot set all ports when SEQOP is disabled.");
        }
        allPorts.writeBytes(value.buffer, 0, value.buffer.length);
    }

    /**
     * Enable interrupts for the selected pins when the input value changes from the default value.
     * @param pins the pins to enable interrupts for.
     * @param defaultValue the default value to compare against.
     */
    public void interruptOnChange(int pins, int defaultValue, GpioEventListener<Mcp23x08> listener) {
        configureInterrupts(pins, state -> {
            state.buffer[DEFVAL] = (byte)defaultValue;      // Default value for pins
            state.buffer[INTCON] |= (byte)pins;             // Compare against default values
        });
        addListener(listener);
    }

    /**
     * Enable interrupts for the selected pins when the input value changes.
     * @param pins the pins to enable interrupts for.
     */
    public void interruptOnChange(int pins, GpioEventListener<Mcp23x08> listener) {
        configureInterrupts(pins, state -> {
            state.buffer[INTCON] &= (byte) (0xff ^ pins);   // Interrupt when changed
        });
        addListener(listener);
    }

    /**
     * Enable interrupts for the selected pins.
     * @param pins the pins to enable interrupts for.
     * @param changeDetectionConfigurer a function to configure the change detection settings.
     */
    private void configureInterrupts(int pins, Consumer<State> changeDetectionConfigurer) {
        Objects.requireNonNull(interruptEventEmitter, "Interrupt event emitter must be set");

        interruptEventEmitter.removeListener(this::handleInterrupt);
        interruptEventEmitter.addListener(this::handleInterrupt);

        State state = read();
        changeDetectionConfigurer.accept(state);
        state.buffer[IOCON] |= 0b100;               // Set interrupt open drain
        state.buffer[IODIR] |= (byte)pins;          // Ensure pins are input
        state.buffer[GPINTEN] |= (byte)pins;        // Interrupt enable pins
        write(state);
    }


    @Override
    public void addListener(GpioEventListener<Mcp23x08> listener) {
        eventEmitterDelegate.addListener(listener);
    }

    @Override
    public void removeListener(GpioEventListener<Mcp23x08> listener) {
        eventEmitterDelegate.removeListener(listener);
    }

    @Override
    public void close() throws Exception {
        this.eventEmitterDelegate.close();
    }

    private void handleInterrupt(GpioChangeEvent<?> event) {
        if (event.eventType() == GpioChangeEventType.FALLING) {
            eventEmitterDelegate.onEvent(new GpioChangeEvent<>(this, event.eventType()));
        }
    }

    /**
     * Container for the all-ports state of the MCP23x08.
     */
    public record State(
            int iodir,
            int ipol,
            int gpinten,
            int defval,
            int intcon,
            int iocon,
            int gppu,
            int intf,
            int intcap,
            int gpio,
            int olat,
            byte[] buffer
    ) {
        public State(byte[] bytes) {
            this(
                    bytes[0] & 0xff,
                    bytes[1] & 0xff,
                    bytes[2] & 0xff,
                    bytes[3] & 0xff,
                    bytes[4] & 0xff,
                    bytes[5] & 0xff,
                    bytes[6] & 0xff,
                    bytes[7] & 0xff,
                    bytes[8] & 0xff,
                    bytes[9] & 0xff,
                    bytes[10] & 0xff,
                    bytes
            );
        }
    }
}
