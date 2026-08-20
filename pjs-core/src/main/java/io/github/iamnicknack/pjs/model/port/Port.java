package io.github.iamnicknack.pjs.model.port;

import io.github.iamnicknack.pjs.model.ReadOperation;
import io.github.iamnicknack.pjs.model.WriteOperation;
import io.github.iamnicknack.pjs.model.pin.Pin;

import java.util.function.Function;

/**
 * A port is a single value, mapped to many pins.
 * <p>
 * I2C, SPI or anything mapped to multiple GPIO pins can be expressed as a port.
 * </p>
 * @param <T> The type used to represent the port value.
 */
public interface Port<T> extends ReadOperation<T>, WriteOperation<T> {
    /**
     * Convert this port to an input-only port.
     * <p>
     * This can be useful for a when we don't want to allow writes to a port or when the underlying
     * hardware doesn't support write operations (e.g. 74xx165 and other PISO devices).
     * </p>
     * @return An input-only port.
     */
    default Port<T> input() {
        return new Port<>() {
            @Override
            public T read() {
                return Port.this.read();
            }

            @Override
            public void write(T value) {
                throw new UnsupportedOperationException("Port is input only");
            }
        };
    }

    /**
     * Convert this port to an output-only port.
     * <p>
     * This can be useful for a when we don't want to allow reads from a port or when the underlying
     * hardware doesn't support read operations (e.g. 74xx595 and other SIPO devices).
     * </p>
     * @return An output-only port.
     */
    default Port<T> output() {
        return new Port<>() {
            @Override
            public T read() {
                throw new UnsupportedOperationException("Port is output only");
            }

            @Override
            public void write(T value) {
                Port.this.write(value);
            }
        };
    }

    /**
     * Map the port value to another type.
     * @param readMapper Function to convert the port value to another type.
     * @param writeMapper Function to convert the new value to the port value.
     * @return A new port with the mapped type
     * @param <V> Type of the mapped port value.
     */
    default <V> Port<V> mapped(Function<T, V> readMapper, Function<V, T> writeMapper) {
        return new Port<>() {
            @Override
            public V read() {
                return readMapper.apply(Port.this.read());
            }

            @Override
            public void write(V value) {
                Port.this.write(writeMapper.apply(value));
            }
        };
    }

    /**
     * Create a pin which provides a 2-state view of this port.
     * @param high Value to write to the port when the pin is set high.
     * @param low Value to write to the port when the pin is set low.
     * @return A pin which controls this port.
     */
    default Pin pin(T high, T low) {
        return new Pin() {
            @Override
            public void high() {
                Port.this.write(high);
            }

            @Override
            public void low() {
                Port.this.write(low);
            }

            @Override
            public Boolean read() {
                return Port.this.read() == high;
            }

            @Override
            public void write(Boolean value) {
                if (value) {
                    high();
                } else {
                    low();
                }
            }
        };
    }

    /**
     * Create a port composed of a read and write delegate.
     * @param readDelegate delegate read operation.
     * @param writeDelegate delegate write operation.
     * @return A new port.
     * @param <T> Type of the port value.
     */
    static <T> Port<T> composite(ReadOperation<T> readDelegate, WriteOperation<T> writeDelegate) {
        return new Port<>() {
            @Override
            public T read() {
                return readDelegate.read();
            }

            @Override
            public void write(T value) {
                writeDelegate.write(value);
            }
        };
    }
}
