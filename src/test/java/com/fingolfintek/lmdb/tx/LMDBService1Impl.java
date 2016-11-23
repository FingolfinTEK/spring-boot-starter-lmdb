package com.fingolfintek.lmdb.tx;

import com.fingolfintek.lmdb.LMDBTemplate;
import org.fusesource.lmdbjni.Database;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import static com.fingolfintek.lmdb.tx.LMDBTransactionManagerIntegrationTest.KEY_1;
import static com.fingolfintek.lmdb.tx.LMDBTransactionManagerIntegrationTest.VALUE_1;


@Service
@LMDBTransactional
public class LMDBService1Impl implements LMDBService1 {

    private final LMDBTemplate lmdb;
    private final Database db;

    public LMDBService1Impl(LMDBTemplate lmdb, Database db) {
        this.lmdb = lmdb;
        this.db = db;
    }

    @Override
    public void writeSomething() {  
        lmdb.iterate(db, Object.class).forEach(e -> lmdb.delete(db, e._1));
        lmdb.write(db, KEY_1, VALUE_1);
    }

    @Override
    @LMDBTransactional(propagation = Propagation.REQUIRES_NEW)
    public void writeSomethingWithNewTransaction() {
        writeSomething();
    }

    @Override
    public void writeSomethingWithErrors() {
        writeSomething();
        throw new RuntimeException("simulate error for rollback");
    }

    @Override
    @LMDBTransactional(readOnly = true)
    public Object readSomething() {
        return lmdb.get(db, KEY_1);
    }
}
