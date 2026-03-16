package io.github.iamnicknack.pjs.sandbox.device.sh1106.impl;

import io.github.iamnicknack.pjs.sandbox.device.sh1106.DisplayOperations;
import io.github.iamnicknack.pjs.sandbox.device.sh1106.buffer.BufferedDisplayOperations;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link BufferedDisplayOperations} implementation which tracks which blocks of data have been modified.
 * <p>
 * The physical display is eight pages by 128 columns. The dirty tracker divides each page into an arbitrary
 * number of "blocks", each containing a specified number of columns, dictated by the block size.
 * <p>
 * For example, with a block size of 16, each page is divided into 8 blocks of 16 columns each. The entire display
 * is divided into 8 pages, so there are 8 * 8 = 64 blocks in total.
 * <p>
 * When a redraw is requested, rather than redrawing the entire display and sending 1024 bytes of data for every frame,
 * only the blocks which contain modified data are redrawn. This can significantly reduce the amount of serial data
 * that needs to be sent to the display, improving performance.
 * <p>
 * If a single pixel on the display is updated, only the block containing that pixel is redrawn. With a block size of
 * 16, for example, only 16 bytes of data need to be sent to the display. In practice, an additional 6 bytes are
 * required to specify the page and column for the block start position. Contiguous blocks per page only need to send
 * this data for the first block as the position for later blocks is implied by the end position of the previous block.
 * <p>
 */
public class DirtyTrackingDisplayBuffer implements BufferedDisplayOperations {

    /**
     * Delegate operations
     */
    private final BufferedDisplayOperations delegate;

    /**
     * Container for dirty block tracking
     */
    private final DirtyDisplay dirtyDisplay;

    /**
     * Constructor with default block size
     */
    public DirtyTrackingDisplayBuffer() {
        this(BLOCK_SIZE);
    }

    /**
     * Constructor with custom block size
     * @param blockSize the block size
     */
    public DirtyTrackingDisplayBuffer(int blockSize) {
        this(new DefaultDisplayBuffer(), blockSize);
    }

    /**
     * Constructor with custom delegate and block size
     * @param delegate the delegate operations
     * @param blockSize the block size
     */
    public DirtyTrackingDisplayBuffer(BufferedDisplayOperations delegate, int blockSize) {
        this.delegate = delegate;
        this.dirtyDisplay = new DirtyDisplay(blockSize);
    }

    private DirtyTrackingDisplayBuffer(BufferedDisplayOperations delegate, DirtyDisplay dirtyDisplay) {
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
            // data before modification
            byte[] before = new byte[length - off];
            getData(page, col, before, 0, before.length);
            operation.apply(page, col, data, off, Math.min(dirtyDisplay.blockSize, length - off));
            // data after modification
            byte[] after = new byte[length - off];
            getData(page, col, after, 0, before.length);

            // compare before and after
            boolean isDirty = false;
            for (int i = 0; i < before.length; i++) {
                if (before[i] != after[i]) {
                    isDirty = true;
                    break;
                }
            }

            if (isDirty) {
                dirtyDisplay.blocksIndex[page][col].dirty = true;
            }
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
    public void andNotData(int page, int column, byte[] data, int offset, int length) {
        modifyData(page, column, data, offset, length, this.delegate::andNotData);
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

        List<Range> dirtyRanges = new ArrayList<>();

        for (int p = 0; p < PAGE_COUNT; p++) {
            for (int i = 0; i < dirtyDisplay.blocksPerPage; i++) {
                var block = this.dirtyDisplay.blocks[p][i];
                if (block.dirty) {
                    if (dirtyRanges.isEmpty()) {
                        dirtyRanges.add(block.range);
                    } else {
                        var last = dirtyRanges.getLast();
                        if (block.range.follows(last)) {
                            var concat = last.concat(block.range);
                            dirtyRanges.set(dirtyRanges.size() - 1, concat);
                        } else {
                            dirtyRanges.add(block.range);
                        }
                    }
                    block.dirty = false;
                }
            }
        }

        for (var range : dirtyRanges) {
            var bytes = new byte[range.length];
            this.delegate.getData(range.page, range.column, bytes, 0, range.length);
            other.setData(range.page, range.column, bytes, 0, range.length);
        }
    }

    /**
     * Factory for creating BufferedDisplayOperations instances. Creating instances via a factory can allow
     * instances to share dirty tracking information.
     */
    public interface Factory {
        /**
         * Creates a new BufferedDisplayOperations instance.
         * @param delegate the delegate operations to use.
         * @return a new BufferedDisplayOperations instance.
         */
        BufferedDisplayOperations create(BufferedDisplayOperations delegate);

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
        public DirtyTrackingDisplayBuffer create(BufferedDisplayOperations delegate) {
            return new DirtyTrackingDisplayBuffer(delegate, dirtyDisplay);
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
        /**
         * Dirty status
         */
        boolean dirty;

        /**
         * The range of data affected by this block.
         */
        final Range range;

        public DirtyBlock(Range range) {
            this.range = range;
        }
    }

    /**
     * Container to indicate a range of values
     * @param page the starting page number
     * @param column the starting column number
     * @param length the length of the range
     */
    private record Range(int page, int column, int length) {
        public boolean follows(Range other) {
            return (this.page == other.page) && (this.column == (other.column + other.length));
        }

        public Range concat(Range other) {
            return new Range(this.page, this.column, this.length + other.length);
        }
    }
}
