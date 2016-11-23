package com.fingolfintek.lmdb;

import org.junit.Test;

import static com.fingolfintek.lmdb.ByteTestUtils.bytes;
import static org.assertj.core.api.Assertions.assertThat;


public class KeyTest {
    @Test
    public void asBytes() throws Exception {
        Key key1 = Key.from(1, 2);
        assertThat(key1.asBytes()).containsExactly(
                bytes(0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2));
    }

    @Test
    public void compareTo() throws Exception {
        Key key1 = Key.from(0, 0);
        Key key2 = Key.from(0, 1);
        Key key3 = Key.from(1, 1);

        assertThat(key1.compareTo(key1)).isZero();
        assertThat(key1.compareTo(key2)).isLessThan(0);
        assertThat(key1.compareTo(key3)).isLessThan(0);
        assertThat(key2.compareTo(key3)).isLessThan(0);
        assertThat(key2.compareTo(key1)).isGreaterThan(0);
        assertThat(key3.compareTo(key2)).isGreaterThan(0);
    }

    @Test
    public void from() throws Exception {
        Key key1 = Key.from(1, 2);
        Key key2 = Key.from( bytes(0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2));
        assertThat(key1).isEqualToComparingFieldByField(key2);
    }

}
