package com.fingolfintek.lmdb;

import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.fingolfintek.lmdb.ByteTestUtils.bytes;
import static org.assertj.core.api.Assertions.assertThat;


@RunWith(JMockit.class)
public class KeyGeneratorTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        new MockUp<System>() {
            @Mock
            public long currentTimeMillis() {
                return 1;
            }
        };
    }

    @Test
    public void generateUniqueKey() throws Exception {
        SimpleKeyGenerator generator = new SimpleKeyGenerator();
        assertThat(generator.generateUniqueKey())
                .isEqualToComparingFieldByFieldRecursively(Key.from(1, 1));
        assertThat(generator.generateUniqueKey())
                .isEqualToComparingFieldByFieldRecursively(Key.from(1, 2));
    }

    @Test
    public void generateUniqueRawKey() throws Exception {
        SimpleKeyGenerator generator = new SimpleKeyGenerator();
        assertThat(generator.generateUniqueRawKey())
                .containsExactly(bytes(0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1));
        assertThat(generator.generateUniqueRawKey())
                .containsExactly(bytes(0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2));

    }

}
