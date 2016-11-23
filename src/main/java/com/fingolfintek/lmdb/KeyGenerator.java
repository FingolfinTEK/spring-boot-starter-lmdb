package com.fingolfintek.lmdb;

import com.fingolfintek.lmdb.Key;

import java.util.concurrent.atomic.AtomicInteger;

public class KeyGenerator {

    private AtomicInteger sequence = new AtomicInteger();

    public Key generateUniqueKey() {
        long timestamp = System.currentTimeMillis();
        int sequenceId = sequence.incrementAndGet();
        return Key.from(timestamp, sequenceId);
    }
    
    public byte[] generateUniqueRawKey() {
        return generateUniqueKey().asBytes();
    }
}
