package com.fingolfintek.lmdb.tx;

public interface LMDBService1 {
    void writeSomething();
    
    void writeSomethingWithNewTransaction();
    
    void writeSomethingWithErrors();

    Object readSomething();
}
