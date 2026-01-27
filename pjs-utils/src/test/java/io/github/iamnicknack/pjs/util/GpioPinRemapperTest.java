package io.github.iamnicknack.pjs.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GpioPinRemapperTest {

    @Test
    void identity() {
        var builder = GpioPinRemapper.builder();

        for (int i = 0; i < 32; i++) {
            builder.pin(i, 1 << i);
        }

        var remapper = builder.build();

        for (int i = 0; i < 32; i++) {
            var mapped = remapper.map(1 << i);
            assertThat(mapped).isEqualTo(1 << i);

            var unmapped = remapper.unmap(mapped);
            assertThat(unmapped).isEqualTo(1 << i);
        }
    }

    @Test
    void shiftLeft() {
        var builder = GpioPinRemapper.builder();

        for (int i = 0; i < 32; i++) {
            builder.pin(i, 2 << i);
        }

        var remapper = builder.build();

        for (int i = 0; i < 31; i++) {
            var mapped = remapper.map(1 << i);
            assertThat(mapped).isEqualTo(2 << i);

            var unmapped = remapper.unmap(mapped);
            assertThat(unmapped).isEqualTo(1 << i);
        }
    }

}