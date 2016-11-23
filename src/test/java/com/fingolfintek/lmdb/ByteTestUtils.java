package com.fingolfintek.lmdb;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

import java.nio.ByteBuffer;

public class ByteTestUtils {
    
    public static ByteBuffer buffer(int... data) {
        return ByteBuffer.wrap(bytes(data));
    }

    public static byte[] bytes(int... data) {
        return Bytes.toArray(Ints.asList(data));
    }

}
