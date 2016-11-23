package com.fingolfintek.lmdb.tx;

import com.fingolfintek.lmdb.LMDBTemplate;
import org.fusesource.lmdbjni.Database;
import org.springframework.stereotype.Service;

import static com.fingolfintek.lmdb.tx.LMDBTransactionManagerIntegrationTest.KEY_2;
import static com.fingolfintek.lmdb.tx.LMDBTransactionManagerIntegrationTest.VALUE_2;


@Service
@LMDBTransactional(readOnly = true)
public class LMDBService2Impl implements LMDBService2 {

    private final LMDBService1 service1;
    private final LMDBTemplate lmdb;
    private final Database db;

    public LMDBService2Impl(LMDBService1 service1, LMDBTemplate lmdb, Database db) {
        this.service1 = service1;
        this.lmdb = lmdb;
        this.db = db;
    }

    @Override
    public Object readSomething() {
        return lmdb.get(db, KEY_2);
    }

    @Override
    @LMDBTransactional
    public void writeSomethingWithNestedTransactions() {
        service1.writeSomething();
        lmdb.write(db, KEY_2, VALUE_2);
    }

    @Override
    public void writeSomethingInReadOnlyTransaction() {
        writeSomethingWithNestedTransactions();
    }

    @Override
    public void writeSomethingWithNewNestedTransactionAndErrors() {
        service1.writeSomethingWithNewTransaction();
        throw new RuntimeException("nested, requires new");
    }
}
