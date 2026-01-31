package io.github.iamnicknack.pjs.sandbox.device.sh1106;

import java.util.ArrayList;
import java.util.List;

public class DisplayBuffer implements DisplayOperations {

    /**
     * Current position in the buffer.
     */
    private int position = 0;

    /**
     * A byte array buffer used to maintain display data
     */
    private final byte[] buffer = new byte[BUFFER_SIZE];

    /**
     * Flags indicating that a block is "dirty"
     */
    private final DirtyBlock[] blocks = new DirtyBlock[BLOCK_COUNT];

    /**
     * An index of dirty bits which allows not calculating the block index at runtime
     */
    private final DirtyBlock[] blocksIndex = new DirtyBlock[BUFFER_SIZE];

    public DisplayBuffer() {
        for(int i = 0; i < BLOCK_COUNT; i++) {
            blocks[i] = new DirtyBlock(new Range(i * BLOCK_SIZE, BLOCK_SIZE));
        }
        for (int i = 0; i < BUFFER_SIZE; i++) {
            blocksIndex[i] = blocks[i / BLOCK_SIZE];
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

    /**
     * Get the block index for the given position.
     * @param page the page number
     * @param column the column number
     * @return the block index
     */
    public int getBlockIndex(int page, int column) {
        return ((page * PAGE_SIZE) + column) / BLOCK_SIZE;
    }

    /**
     * Get the position for the given block index.
     * @param blockIndex the block index
     * @return the position
     */
    public int getPositionForBlock(int blockIndex) {
        return blockIndex * BLOCK_SIZE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setData(byte[] data, int offset, int length) {
        var off = offset;

        while (off < length) {
            System.arraycopy(data, off, buffer, position + off, Math.min(BLOCK_SIZE, length - off));
            blocksIndex[position + off].dirty = true;
            off += BLOCK_SIZE;
        }

        position = (position + length) % BUFFER_SIZE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPosition(int page, int column) {
        position = ((page * 128) + column) % 1024;
    }


    /**
     * Copies data from blocks marked as "dirty" to another display buffer.
     * @param other The display to copy into.
     */
    @Override
    public void copyTo(DisplayOperations other) {

        List<DirtyBlock> dirtyBlocks = new ArrayList<>();

        for (int i = 0; i < BLOCK_COUNT; i++) {
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
            other.setData(marker.range.start, buffer, marker.range.start, marker.range.length);
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
