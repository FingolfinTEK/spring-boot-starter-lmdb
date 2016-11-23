package com.fingolfintek.lmdb;


import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.Objects;

import static java.util.Comparator.comparing;

public class Key implements Comparable<Key> {

    private static final Comparator<Key> KEY_COMPARATOR = 
            comparing((Key key) -> key.timestamp).thenComparing((Key key) -> key.sequence);
    
    private final long timestamp;
    private final int sequence;

    private Key(long timestamp, int sequence) {
        this.timestamp = timestamp;
        this.sequence = sequence;
    }

    public byte[] asBytes() {
        return ByteBuffer.allocate(12)
                .putLong(timestamp)
                .putInt(sequence)
                .array();
    }

    @Override
    public int compareTo(Key o) {
        return KEY_COMPARATOR.compare(this, o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, sequence);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Key key = (Key) o;
        return timestamp == key.timestamp &&
                sequence == key.sequence;
    }

    public static Key from(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return from(buffer.getLong(), buffer.getInt());
    }

    public static Key from(long timestamp, int sequence) {
        return new Key(timestamp, sequence);
    }

}
