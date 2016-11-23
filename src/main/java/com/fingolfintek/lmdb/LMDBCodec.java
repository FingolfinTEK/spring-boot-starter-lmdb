package com.fingolfintek.lmdb;

public interface LMDBCodec {

    byte[] marshal(Object value);

    Object unmarshal(byte[] dbValue);

    default <T> T unmarshal(byte[] dbValue, Class<T> type) {
        return type.cast(unmarshal(dbValue));
    }

}
