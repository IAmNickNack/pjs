package io.github.iamnicknack.pjs.device.spi;

/**
 * The implementation is a component capable of creating {@link SpiTransfer} instances.
 */
@FunctionalInterface
public interface SpiTransferProvider {

    /**
     * Create a {@link SpiTransfer} instance using the specified SPI.
     * @param spi the SPI to delegate to.
     * @return the created transfer.
     */
    SpiTransfer createTransfer(Spi spi);
}
