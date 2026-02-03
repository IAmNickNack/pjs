package io.github.iamnicknack.pjs.sandbox.device.sh1106.impl;

import io.github.iamnicknack.pjs.sandbox.device.sh1106.DisplayOperations;

import java.util.ArrayList;
import java.util.List;

public class BufferedDisplayOperations implements DisplayOperations {

    /**
     * Delegate operations
     */
    private final DisplayOperations delegate;

    /**
     * Container for dirty block tracking
     */
    private final DirtyDisplay dirtyDisplay;

    /**
     * Constructor with default block size
     */
    public BufferedDisplayOperations() {
        this(BLOCK_SIZE);
    }

    /**
     * Constructor with custom block size
     * @param blockSize the block size
     */
    public BufferedDisplayOperations(int blockSize) {
        this(new DefaultDisplayBuffer(), blockSize);
    }

    /**
     * Constructor with custom delegate and block size
     * @param delegate the delegate operations
     * @param blockSize the block size
     */
    public BufferedDisplayOperations(DisplayOperations delegate, int blockSize) {
        this.delegate = delegate;
        this.dirtyDisplay = new DirtyDisplay(blockSize);
    }

    private BufferedDisplayOperations(DisplayOperations delegate, DirtyDisplay dirtyDisplay) {
        this.delegate = delegate;
        this.dirtyDisplay = dirtyDisplay;
    }

    /**
     * Check whether the data is dirty at the given position.
     * @param page the page number
     * @param column the column number
     * @return true if the data at the given position is dirty, false otherwise
     */
    public boolean isDirty(int page, int column) {
        return dirtyDisplay.blocksIndex[page][column].dirty;
    }

    private void modifyData(int page, int column, byte[] data, int offset, int length, DataOperation operation) {
        int off = offset;

        while (off < length) {
            int col = (column + off) % PAGE_SIZE;
            operation.apply(page, col, data, off, Math.min(dirtyDisplay.blockSize, length - off));
            dirtyDisplay.blocksIndex[page][col].dirty = true;
            off += dirtyDisplay.blockSize;
        }
    }

    @Override
    public void setData(int page, int column, byte[] data, int offset, int length) {
        modifyData(page, column, data, offset, length, this.delegate::setData);
    }

    @Override
    public void orData(int page, int column, byte[] data, int offset, int length) {
        modifyData(page, column, data, offset, length, this.delegate::orData);
    }

    @Override
    public void andData(int page, int column, byte[] data, int offset, int length) {
        modifyData(page, column, data, offset, length, this.delegate::andData);
    }

    @Override
    public void xorData(int page, int column, byte[] data, int offset, int length) {
        modifyData(page, column, data, offset, length, this.delegate::xorData);
    }

    @Override
    public void getData(int page, int column, byte[] buffer, int offset, int length) {
        this.delegate.getData(page, column, buffer, offset, length);
    }

    @Override
    public int getPointValue(int page, int column) {
        return this.delegate.getPointValue(page, column);
    }

    /**
     * Copies data from blocks marked as "dirty" to another display buffer.
     * @param other The display to copy into.
     */
    @Override
    public void copyTo(DisplayOperations other) {

        List<DirtyBlock> dirtyBlocks = new ArrayList<>();

        for (int p = 0; p < PAGE_COUNT; p++) {
            for (int i = 0; i < dirtyDisplay.blocksPerPage; i++) {
                var block = this.dirtyDisplay.blocks[p][i];
                if (block.dirty) {
                    if (dirtyBlocks.isEmpty()) {
                        dirtyBlocks.add(block);
                    } else {
                        var last = dirtyBlocks.getLast();
                        if (block.follows(last)) {
                            var concat = last.concat(block);
                            dirtyBlocks.set(dirtyBlocks.size() - 1, concat);
                        } else {
                            dirtyBlocks.add(block);
                        }
                    }
                    block.dirty = false;
                }
            }
        }

        for (var marker : dirtyBlocks) {
            var bytes = new byte[marker.range.length];
            this.delegate.getData(marker.range.page, marker.range.column, bytes, 0, marker.range.length);
            other.setData(marker.range.page, marker.range.column, bytes, 0, marker.range.length);
        }
    }

    /**
     * Factory for creating BufferedDisplayOperations instances
     */
    public interface Factory {
        /**
         * Creates a new BufferedDisplayOperations instance.
         * @param delegate the delegate operations to use.
         * @return a new BufferedDisplayOperations instance.
         */
        BufferedDisplayOperations create(DisplayOperations delegate);

        /**
         * Create a new factory with the default block size.
         * @return a new factory.
         */
        static Factory newDefault() {
            return new DefaultFactory(BLOCK_SIZE);
        }

        /**
         * Create a new factory with the given block size.
         * @param blockSize the block size to use.
         * @return a new factory.
         */
        static Factory newDefault(int blockSize) {
            return new DefaultFactory(blockSize);
        }
    }

    /**
     * Default factory implementation which shares a single DirtyDisplay instance between all instances created.
     */
    public static class DefaultFactory implements Factory {

        private final DirtyDisplay dirtyDisplay;

        public DefaultFactory(int blockSize) {
            this.dirtyDisplay = new DirtyDisplay(blockSize);
        }

        @Override
        public BufferedDisplayOperations create(DisplayOperations delegate) {
            return new BufferedDisplayOperations(delegate, dirtyDisplay);
        }
    }

    /**
     * Container for dirty information.
     */
    private static class DirtyDisplay {
       /**
         * Flags indicating that a block is "dirty"
         */
        private final DirtyBlock[][] blocks;

        /**
         * An index of dirty bits which allows not calculating the block index at runtime
         */
        private final DirtyBlock[][] blocksIndex = new DirtyBlock[PAGE_COUNT][PAGE_SIZE];

        /**
         * The number of blocks per page
         */
        private final int blocksPerPage;

        /**
         * The number of columns in a block
         */
        private final int blockSize;

        /**
         * Initialise a new instance with the given block size.
         * @param blockSize the block size to use.
         */
        private DirtyDisplay(int blockSize) {
            this.blockSize = blockSize;
            this.blocksPerPage = PAGE_SIZE / blockSize;

            this.blocks = new DirtyBlock[PAGE_COUNT][];

            for (int p = 0; p < PAGE_COUNT; p++) {
                this.blocks[p] = new DirtyBlock[blocksPerPage];
                for (int i = 0; i < blocksPerPage; i++) {
                    this.blocks[p][i] = new DirtyBlock(new Range(p, i * blockSize, blockSize));
                }

                blocksIndex[p] = new DirtyBlock[PAGE_SIZE];
                for (int i = 0; i < PAGE_SIZE; i++) {
                    blocksIndex[p][i] = blocks[p][i / blockSize];
                }
            }
        }

    }

    /**
     * Represents a marker for tracking the "dirty" status of a specific range.
     * Used internally to indicate which portions of data have been modified.
     */
    private static class DirtyBlock {
        boolean dirty;
        final Range range;

        public DirtyBlock(Range range) {
            this.range = range;
        }

        public boolean follows(DirtyBlock other) {
            return (this.range.page == other.range.page) &&
                    (this.range.column == (other.range.column + other.range.length));
        }

        public DirtyBlock concat(DirtyBlock other) {
            return new DirtyBlock(new Range(this.range.page, this.range.column, this.range.length + other.range.length));
        }
    }

    /**
     * Container to indicate a range of values
     * @param page the starting page number
     * @param column the starting column number
     * @param length the length of the range
     */
    private record Range(int page, int column, int length) {}
}
