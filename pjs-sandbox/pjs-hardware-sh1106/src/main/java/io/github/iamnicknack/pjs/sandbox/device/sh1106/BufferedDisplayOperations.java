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
    private final DirtyBlock[] blocks;

    /**
     * An index of dirty bits which allows not calculating the block index at runtime
     */
    private final DirtyBlock[] blocksIndex = new DirtyBlock[BUFFER_SIZE];

    private final int blockCount;

    private final int blockSize;

    public BufferedDisplayOperations() {
        this(BLOCK_SIZE);
    }

    public BufferedDisplayOperations(int blockSize) {
        this(new DefaultDisplayBuffer(), blockSize);
    }

    public BufferedDisplayOperations(DisplayOperations delegate, int blockSize) {
        this.blockSize = blockSize;
        this.blockCount = BUFFER_SIZE / blockSize;
        this.blocks = new DirtyBlock[blockCount];
        this.delegate = delegate;

        for(int i = 0; i < blockCount; i++) {
            blocks[i] = new DirtyBlock(new Range(i * blockSize, blockSize));
        }
        for (int i = 0; i < BUFFER_SIZE; i++) {
            blocksIndex[i] = blocks[i / blockSize];
        }
    }

    /**
     * Check whether the data is dirty at the given position.
     * @param page the page number
     * @param column the column number
     * @return true if the data at the given position is dirty, false otherwise
     */
    public boolean isDirty(int page, int column) {
        return blocksIndex[(page * PAGE_SIZE) + column].dirty;
    }

    @Override
    public void setData(int position, byte[] data, int offset, int length) {
        var off = offset;

        while (off < length) {
            this.delegate.setData(position + off, data, off, Math.min(blockSize, length - off));
            blocksIndex[position + off].dirty = true;
            off += blockSize;
        }
    }

    @Override
    public void getData(int position, byte[] buffer, int offset, int length) {
        this.delegate.getData(position, buffer, offset, length);
    }

    @Override
    public int getPointValue(int position) {
        return this.delegate.getPointValue(position);
    }

    /**
     * Copies data from blocks marked as "dirty" to another display buffer.
     * @param other The display to copy into.
     */
    @Override
    public void copyTo(DisplayOperations other) {

        List<DirtyBlock> dirtyBlocks = new ArrayList<>();

        for (int i = 0; i < blockCount; i++) {
            if (this.blocks[i].dirty) {
                if (dirtyBlocks.isEmpty()) {
                    dirtyBlocks.add(this.blocks[i]);
                } else {
                    var last = dirtyBlocks.getLast();
                    if (this.blocks[i].follows(last)) {
                        dirtyBlocks.set(dirtyBlocks.size() - 1, last.concat(this.blocks[i]));
                    } else {
                        dirtyBlocks.add(this.blocks[i]);
                    }
                }
                this.blocks[i].dirty = false;
            }
        }

        for (var marker : dirtyBlocks) {
            var bytes = new byte[marker.range.length];
            this.delegate.getData(marker.range.start, bytes, 0, marker.range.length);
            other.setData(marker.range.start, bytes, 0, marker.range.length);
        }
    }

    /**
     * Represents a marker for tracking the "dirty" status of a specific range.
     * Used internally to indicate which portions of data have been modified.
     */
    private static class DirtyBlock {
        boolean dirty;
        final Range range;
        final int page;

        public DirtyBlock(Range range) {
            this.range = range;
            this.page = range.start / PAGE_SIZE;
        }

        public boolean follows(DirtyBlock other) {
            return (this.page == other.page) &&
                    (this.range.start == (other.range.start + other.range.length));
        }

        public DirtyBlock concat(DirtyBlock other) {
            return new DirtyBlock(new Range(this.range.start, this.range.length + other.range.length));
        }
    }

    /**
     * Container to indicate a range of values
     * @param start the start of the range
     * @param length the length of the range
     */
    private record Range(int start, int length) {}
}
