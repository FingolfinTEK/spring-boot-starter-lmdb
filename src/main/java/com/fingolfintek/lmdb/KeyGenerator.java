package com.fingolfintek.lmdb;

public interface KeyGenerator {
    
    Key generateUniqueKey();

    default byte[] generateUniqueRawKey() {
        return generateUniqueKey().asBytes();
    }
}
