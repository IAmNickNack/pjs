package io.github.iamnicknack.pjs.sandbox.device.eeprom;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class PageFunctionTest {

    @Test
    void canCreatePageData() {
        var pageFunction = new PageFunction.DefaultPageFunction(32);
        var data = new byte[] { 1, 2, 3 };
        var iterator = pageFunction.pages(0, data);

        var next = iterator.next();
        assertThat(iterator.hasNext()).isFalse();

        assertThat(next.address()).isEqualTo(0);
        assertThat(next.data()).hasSize(3);
        assertThat(next.data()).containsExactly(data);
    }

    @Test
    void canCreatePageDataCrossingPageBoundary() {
        var pageFunction = new PageFunction.DefaultPageFunction(32);
        var data = new byte[] { 1, 2, 3 };
        var iterator = pageFunction.pages(30, data);

        var firstPage = iterator.next();
        var secondPage = iterator.next();
        assertThat(iterator.hasNext()).isFalse();

        assertThat(firstPage.address()).isEqualTo(30);
        assertThat(firstPage.data()).hasSize(2);
        assertThat(firstPage.data()).containsExactly(new byte[] { 1, 2 } );

        assertThat(secondPage.address()).isEqualTo(32);
        assertThat(secondPage.data()).hasSize(1);
        assertThat(secondPage.data()).containsExactly(new byte[] { 3 } );
    }

    @Test
    void canCreateMultiplePages() {
        var pageFunction = new PageFunction.DefaultPageFunction(32);
        var data = new byte[33];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
        }
        var iterator = pageFunction.pages(0, data);

        var firstPage = iterator.next();
        var secondPage = iterator.next();

        assertThat(firstPage.address()).isEqualTo(0);
        assertThat(firstPage.data()).hasSize(32);

        assertThat(secondPage.address()).isEqualTo(32);
        assertThat(secondPage.data()).hasSize(1);
        assertThat(secondPage.data()).containsExactly(new byte[] { 32 } );
    }

    @Test
    void canCreateMultiplePagesWithOffset() {
        var pageFunction = new PageFunction.DefaultPageFunction(32);
        var data = new byte[64];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
        }
        var iterator = pageFunction.pages(10, data);

        var firstPage = iterator.next();
        var secondPage = iterator.next();
        var thirdPage = iterator.next();
        assertThat(iterator.hasNext()).isFalse();

        assertThat(firstPage.address()).isEqualTo(10);
        assertThat(firstPage.data()).hasSize(22);
        assertThat(firstPage.data()[1]).isEqualTo((byte)1);

        assertThat(secondPage.address()).isEqualTo(32);
        assertThat(secondPage.data()).hasSize(32);
        assertThat(secondPage.data()[0]).isEqualTo((byte)22);

        assertThat(thirdPage.address()).isEqualTo(64);
        assertThat(thirdPage.data()).hasSize(10);
        assertThat(thirdPage.data()[0]).isEqualTo((byte)54);
    }

    @Test
    void canWrapPages() {
        var pageFunction = new PageFunction.DefaultPageFunction(32);
        var data = new byte[] { 1, 2, 3, 4 };
        var iterator = pageFunction.pages(61, data);

        var list = new ArrayList<PageFunction.PageData>();
        iterator.forEachRemaining(list::add);
        assertThat(list).hasSize(2);
        assertThat(list.get(0).address()).isEqualTo(61);
        assertThat(list.get(1).address()).isEqualTo(64);
    }

}