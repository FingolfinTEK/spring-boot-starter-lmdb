package com.fingolfintek.lmdb;

import java.util.concurrent.atomic.AtomicInteger;

public class SimpleKeyGenerator implements KeyGenerator {

    private AtomicInteger sequence = new AtomicInteger();

    @Override
    public Key generateUniqueKey() {
        long timestamp = System.currentTimeMillis();
        int sequenceId = sequence.incrementAndGet();
        return Key.from(timestamp, sequenceId);
    }

}
