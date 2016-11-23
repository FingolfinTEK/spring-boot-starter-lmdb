package com.fingolfintek.lmdb.tx;

public interface LMDBService2 {
    void writeSomethingWithNestedTransactions();

    void writeSomethingInReadOnlyTransaction();
    
    void writeSomethingWithNewNestedTransactionAndErrors();

    Object readSomething();
}
