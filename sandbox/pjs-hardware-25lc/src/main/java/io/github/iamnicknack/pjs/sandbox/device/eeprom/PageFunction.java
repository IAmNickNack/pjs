package io.github.iamnicknack.pjs.sandbox.device.eeprom;

import java.util.Iterator;

/**
 * An operation able to split a byte array into segments of a given size based on a start address.
 */
@FunctionalInterface
public interface PageFunction {

    /**
     * Generate blocks of data from the given byte array, which can be written to the device.
     * @param startAddress the address to start writing from.
     * @param bytes the data to write.
     * @return an iterator of pages to write.
     */
    Iterator<PageData> pages(int startAddress, byte[] bytes);

    /**
     * A container for a page of data.
     * @param address the start address of data.
     * @param data the data to write.
     */
    record PageData(int address, byte[] data) {}

    class DefaultPageFunction implements PageFunction {

        private final int pageSize;
        private final int pageMask;
        private final int offsetMask;

        public DefaultPageFunction(int pageSize) {
            this.pageSize = pageSize;
            this.offsetMask = pageSize - 1;
            this.pageMask = 0xFFFF &~ offsetMask;
        }

        @Override
        public Iterator<PageData> pages(int startAddress, byte[] bytes) {

            return new Iterator<>() {
                private int offset = 0;
                private int lastAddress = startAddress;
                private int nextAddress = (startAddress & pageMask) + pageSize;

                @Override
                public boolean hasNext() {
                    return offset < bytes.length;
                }

                @Override
                public PageData next() {
                    int address = lastAddress;
                    int length = Math.min(pageSize - (address & offsetMask), bytes.length - offset);
                    byte[] data = new byte[length];
                    System.arraycopy(bytes, offset, data, 0, length);

                    offset += length;
                    lastAddress = nextAddress;
                    nextAddress = (nextAddress & pageMask) + pageSize;

                    return new PageData(address, data);
                }
            };
        }
    }
}
