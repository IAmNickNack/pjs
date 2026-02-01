package io.github.iamnicknack.pjs.sandbox.device.sh1106;

import java.util.ArrayList;
import java.util.List;

public class BufferedDisplayOperations implements DisplayOperations {

    /**
     * Delegate operations
     */
    private final DisplayOperations delegate;

    /**
     * Flags indicating that a block is "dirty"
     */
    private final DirtyBlock[][] blocks;

    /**
     * An index of dirty bits which allows not calculating the block index at runtime
     */
    private final DirtyBlock[][] blocksIndex = new DirtyBlock[PAGE_COUNT][PAGE_SIZE];

    private final int blocksPerPage;

    private final int blockSize;

    public BufferedDisplayOperations() {
        this(BLOCK_SIZE);
    }

    public BufferedDisplayOperations(int blockSize) {
        this(new DefaultDisplayBuffer(), blockSize);
    }

    public BufferedDisplayOperations(DisplayOperations delegate, int blockSize) {
        this.delegate = delegate;
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

    /**
     * Check whether the data is dirty at the given position.
     * @param page the page number
     * @param column the column number
     * @return true if the data at the given position is dirty, false otherwise
     */
    public boolean isDirty(int page, int column) {
        return blocksIndex[page][column].dirty;
    }

    @Override
    public void setData(int page, int column, byte[] data, int offset, int length) {
        int off = offset;

        while (off < length) {
            int col = (column + off) % PAGE_SIZE;
            this.delegate.setData(page, col, data, off, Math.min(blockSize, length - off));
            blocksIndex[page][col].dirty = true;
            off += blockSize;
        }
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
            for (int i = 0; i < blocksPerPage; i++) {
                var block = this.blocks[p][i];
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
