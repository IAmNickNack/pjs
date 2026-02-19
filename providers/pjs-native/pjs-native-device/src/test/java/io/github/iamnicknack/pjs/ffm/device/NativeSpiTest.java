package io.github.iamnicknack.pjs.ffm.device;

import io.github.iamnicknack.pjs.device.spi.Spi;
import io.github.iamnicknack.pjs.device.spi.SpiConfig;
import io.github.iamnicknack.pjs.device.spi.SpiProvider;
import io.github.iamnicknack.pjs.device.spi.SpiTransfer;
import io.github.iamnicknack.pjs.ffm.context.segment.MemorySegmentMapperImpl;
import io.github.iamnicknack.pjs.ffm.device.context.AbstractIoctlOperations;
import io.github.iamnicknack.pjs.ffm.device.context.VirtualFileOperations;
import io.github.iamnicknack.pjs.ffm.device.context.spi.SpiConstants;
import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

import static io.github.iamnicknack.pjs.ffm.device.context.spi.SpiConstants.SPI_IOC_RD_BITS_PER_WORD;
import static io.github.iamnicknack.pjs.ffm.device.context.spi.SpiConstants.SPI_IOC_RD_LSB_FIRST;
import static io.github.iamnicknack.pjs.ffm.device.context.spi.SpiConstants.SPI_IOC_RD_MAX_SPEED_HZ;
import static io.github.iamnicknack.pjs.ffm.device.context.spi.SpiConstants.SPI_IOC_RD_MODE;
import static io.github.iamnicknack.pjs.ffm.device.context.spi.SpiConstants.SPI_IOC_WR_BITS_PER_WORD;
import static io.github.iamnicknack.pjs.ffm.device.context.spi.SpiConstants.SPI_IOC_WR_LSB_FIRST;
import static io.github.iamnicknack.pjs.ffm.device.context.spi.SpiConstants.SPI_IOC_WR_MAX_SPEED_HZ;
import static io.github.iamnicknack.pjs.ffm.device.context.spi.SpiConstants.SPI_IOC_WR_MODE;

class NativeSpiTest {
    public static final UnaryOperator<AbstractIoctlOperations.Builder> DEFAULT_IOCTL_HANDLERS = builder -> builder
            .addHandler(SPI_IOC_RD_MODE)
            .addHandler(SPI_IOC_WR_MODE)
            .addHandler(SPI_IOC_RD_BITS_PER_WORD)
            .addHandler(SPI_IOC_WR_BITS_PER_WORD)
            .addHandler(SPI_IOC_RD_MAX_SPEED_HZ)
            .addHandler(SPI_IOC_WR_MAX_SPEED_HZ)
            .addHandler(SPI_IOC_RD_LSB_FIRST)
            .addHandler(SPI_IOC_WR_LSB_FIRST);

    @Test
    void canTransfer() {
        performTest(
                builder -> builder.addHandler(SpiConstants.SPI_IOC_MESSAGE(1)),
                (spi, _) -> spi
                        .transfer(new byte[0], 0, new byte[0], 0, 0)
        );
    }

    @Test
    void canTransferMessage() {
        performTest(
                builder -> builder.addHandler(SpiConstants.SPI_IOC_MESSAGE(1)),
                (spi, provider) -> provider
                        .createTransfer(spi)
                        .transfer(SpiTransfer.Message.write(new byte[0], 0, 0))
        );
    }

    private void performTest(
            UnaryOperator<AbstractIoctlOperations.Builder> ioctlHandlers,
            BiConsumer<Spi, SpiProvider> verifier
    ) {
        var fileOperations = new VirtualFileOperations();
        var ioctlOperations = AbstractIoctlOperations.builder()
                .add(DEFAULT_IOCTL_HANDLERS)
                .add(ioctlHandlers)
                .build();
        var segmentAllocator = Arena.ofAuto();
        var mapper = new MemorySegmentMapperImpl(segmentAllocator);
        var config = SpiConfig.builder().chipSelect(0).bus(0).build();

        try (var provider = new NativeSpiProvider(fileOperations, ioctlOperations, mapper, segmentAllocator);
             var spi = provider.create(config)) {
            verifier.accept(spi, provider);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
