package io.github.iamnicknack.pjs.ffm.device.context.spi;

import io.github.iamnicknack.pjs.ffm.context.segment.DeserializeUsing;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentDeserializer;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentSerializer;
import io.github.iamnicknack.pjs.ffm.context.segment.SerializeUsing;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.MemoryLayout.PathElement.groupElement;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_SHORT;

/**
 * @param txBuf Holds pointer to userspace buffer with transmit data, or null.
 *	If no data is provided, zeroes are shifted out.
 * @param rxBuf Holds pointer to userspace buffer for receive data, or null.
 * @param length Length of tx and rx buffers, in bytes.
 * @param speedHz Temporary override of the device's bitrate.
 * @param bitsPerWord Temporary override of the device's wordsize.
 * @param delayUsecs If nonzero, how long to delay after the last bit transfer
 *	before optionally deselecting the device before the next transfer.
 * @param csChange True to deselect device before starting the next transfer message.
 * @param wordDelayUsecs If nonzero, how long to wait between words within one
 *	transfer. This property needs explicit support in the SPI controller,
 *	otherwise it is silently ignored.
 */
@SerializeUsing(SpiTransfer.Serializer.class)
@DeserializeUsing(SpiTransfer.Deserializer.class)
public record SpiTransfer(
        MemorySegment txBuf,
        MemorySegment rxBuf,
        int length,
        int speedHz,
        short delayUsecs,
        byte bitsPerWord,
        byte csChange,
        byte txNbits,
        byte rxNbits,
        byte wordDelayUsecs
) {

    public static Builder builder() {
         return new Builder();
    }

    /**
     * Builder class to avoid secondary constructors for as-yet-unknown use cases.
     */
    public static class Builder {
        private MemorySegment txBuf = MemorySegment.NULL;
        private MemorySegment rxBuf = MemorySegment.NULL;
        private int length = 0;
        private int speedHz = 0;
        private int delayUsecs = 0;
        private int bitsPerWord = 0;
        private boolean csChange = false;
        private int txNbits = 0;
        private int rxNbits = 0;
        private int wordDelayUsecs = 0;

        public Builder txBuf(MemorySegment txBuf) {
            this.txBuf = txBuf;
            return this;
        }

        public Builder rxBuf(MemorySegment rxBuf) {
            this.rxBuf = rxBuf;
            return this;
        }

        public Builder length(int length) {
            this.length = length;
            return this;
        }

        public Builder speedHz(int speedHz) {
            this.speedHz = speedHz;
            return this;
        }

        public Builder delayUsecs(int delayUsecs) {
            this.delayUsecs = delayUsecs;
            return this;
        }

        public Builder bitsPerWord(int bitsPerWord) {
            this.bitsPerWord = bitsPerWord;
            return this;
        }

        public Builder csChange(boolean csChange) {
            this.csChange = csChange;
            return this;
        }

        public Builder txNbits(int txNbits) {
            this.txNbits = txNbits;
            return this;
        }

        public Builder rxNbits(int rxNbits) {
            this.rxNbits = rxNbits;
            return this;
        }

        public Builder wordDelayUsecs(int wordDelayUsecs) {
            this.wordDelayUsecs = wordDelayUsecs;
            return this;
        }

        public SpiTransfer build() {
            return new SpiTransfer(txBuf, rxBuf, length, speedHz, (short)delayUsecs, (byte)bitsPerWord, csChange ? (byte)1 : (byte)0, (byte)txNbits, (byte)rxNbits, (byte)wordDelayUsecs);
        }
    }

    public static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
            ADDRESS.withName("txBuf"),
            ADDRESS.withName("rxBuf"),
            JAVA_INT.withName("length"),
            JAVA_INT.withName("speedHz"),
            JAVA_SHORT.withName("delayUsecs"),
            JAVA_BYTE.withName("bitsPerWord"),
            JAVA_BYTE.withName("csChange"),
            JAVA_BYTE.withName("txNbits"),
            JAVA_BYTE.withName("rxNbits"),
            JAVA_BYTE.withName("wordDelayUsecs"),
            JAVA_BYTE.withName("pad")
    );

    private static final VarHandle VH_TX_BUF = LAYOUT.varHandle(groupElement("txBuf"));
    private static final VarHandle VH_RX_BUF = LAYOUT.varHandle(groupElement("rxBuf"));
    private static final VarHandle VH_LEN = LAYOUT.varHandle(groupElement("length"));
    private static final VarHandle VH_SPEED_HZ = LAYOUT.varHandle(groupElement("speedHz"));
    private static final VarHandle VH_DELAY_USECS = LAYOUT.varHandle(groupElement("delayUsecs"));
    private static final VarHandle VH_BITS_PER_WORD = LAYOUT.varHandle(groupElement("bitsPerWord"));
    private static final VarHandle VH_CS_CHANGE = LAYOUT.varHandle(groupElement("csChange"));
    private static final VarHandle VH_TX_NBITS = LAYOUT.varHandle(groupElement("txNbits"));
    private static final VarHandle VH_RX_NBITS = LAYOUT.varHandle(groupElement("rxNbits"));
    private static final VarHandle VH_WORD_DELAY_USECS = LAYOUT.varHandle(groupElement("wordDelayUsecs"));
    private static final VarHandle VH_PAD = LAYOUT.varHandle(groupElement("pad"));

    public byte[] txArray() {
        return txBuf.toArray(JAVA_BYTE);
    }

    public byte[] rxArray() {
        return rxBuf.toArray(JAVA_BYTE);
    }

    public static class Serializer implements MemorySegmentSerializer<SpiTransfer> {

        private final SegmentAllocator segmentAllocator;

        public Serializer(SegmentAllocator segmentAllocator) {
            this.segmentAllocator = segmentAllocator;
        }

        @Override
        public MemoryLayout layout() {
            return LAYOUT;
        }

        @Override
        public MemorySegment serialize(SpiTransfer transferBuffer) {
            var segment = segmentAllocator.allocate(LAYOUT);
            VH_TX_BUF.set(segment, 0L, transferBuffer.txBuf);
            VH_RX_BUF.set(segment, 0L, transferBuffer.rxBuf);
            VH_LEN.set(segment, 0L, transferBuffer.length);
            VH_SPEED_HZ.set(segment, 0L, transferBuffer.speedHz);
            VH_DELAY_USECS.set(segment, 0L, transferBuffer.delayUsecs);
            VH_BITS_PER_WORD.set(segment, 0L, transferBuffer.bitsPerWord);
            VH_CS_CHANGE.set(segment, 0L, transferBuffer.csChange);
            VH_TX_NBITS.set(segment, 0L, transferBuffer.txNbits);
            VH_RX_NBITS.set(segment, 0L, transferBuffer.rxNbits);
            VH_WORD_DELAY_USECS.set(segment, 0L, transferBuffer.wordDelayUsecs);
            VH_PAD.set(segment, 0L, (byte)0);
            return segment;
        }
    }

    /**
     * Useful only for debugging and testing as serialisation does not track memory segment references.
     * Tests can use this to verify that the data was correctly written to the memory segments.
     */
    public static class Deserializer implements MemorySegmentDeserializer<SpiTransfer> {

        @Override
        public MemoryLayout layout() {
            return LAYOUT;
        }

        @Override
        public SpiTransfer deserialize(MemorySegment segment) {
            return new SpiTransfer(
                    (MemorySegment) VH_TX_BUF.get(segment, 0L),
                    (MemorySegment) VH_RX_BUF.get(segment, 0L),
                    (int) VH_LEN.get(segment, 0L),
                    (int) VH_SPEED_HZ.get(segment, 0L),
                    (short) VH_DELAY_USECS.get(segment, 0L),
                    (byte) VH_BITS_PER_WORD.get(segment, 0L),
                    (byte) VH_CS_CHANGE.get(segment, 0L),
                    (byte) VH_TX_NBITS.get(segment, 0L),
                    (byte) VH_RX_NBITS.get(segment, 0L),
                    (byte) VH_WORD_DELAY_USECS.get(segment, 0L)
            );
        }
    }
}
