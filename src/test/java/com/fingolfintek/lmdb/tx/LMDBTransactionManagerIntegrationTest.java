package com.fingolfintek.lmdb.tx;

import com.fingolfintek.lmdb.BaseIntegrationTest;
import com.fingolfintek.lmdb.Key;
import com.fingolfintek.lmdb.LambdaUtils;
import org.fusesource.lmdbjni.Database;
import org.fusesource.lmdbjni.Env;
import org.fusesource.lmdbjni.LMDBException;
import org.fusesource.lmdbjni.Transaction;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Rollback
public class LMDBTransactionManagerIntegrationTest extends BaseIntegrationTest {

    static final Key KEY_1 = Key.from(1, 1);
    static final Key KEY_2 = Key.from(2, 2);
    static final String VALUE_1 = "Initial value 1";
    static final String VALUE_2 = "Initial value 2";

    @Autowired
    private Env env;

    @Autowired
    private Database db;

    @Autowired
    private LMDBService1 service1;

    @Autowired
    private LMDBService2 service2;

    @After
    public void cleanDatabase() {
        try (Transaction tx = env.createReadTransaction()) {
            LambdaUtils.create(db.iterate(tx)).forEach(e -> db.delete(e.getKey()));
        }
    }

    @Test
    public void transactionalReadAndWrite() throws Exception {
        service1.writeSomething();
        Object value = service1.readSomething();
        assertThat(value).isEqualTo(VALUE_1);
    }

    @Test
    public void nestedWriteTransactions() throws Exception {
        service2.writeSomethingWithNestedTransactions();
        assertThat(service1.readSomething()).isEqualTo(VALUE_1);
        assertThat(service2.readSomething()).isEqualTo(VALUE_2);
    }

    @Test
    public void writeWithReadOnlyTransaction() throws Exception {
        assertThatThrownBy(() -> service2.writeSomethingInReadOnlyTransaction())
                .isExactlyInstanceOf(LMDBException.class)
                .hasMessage("Permission denied");
    }

    @Test
    public void writeWithErrorsAndRollback() throws Exception {
        assertThatThrownBy(() -> service1.writeSomethingWithErrors())
                .isExactlyInstanceOf(RuntimeException.class);
        assertThat(service1.readSomething()).isNull();
    }

    @Test
    public void writeWithNewNestedTransactionAndErrors() throws Exception {
        assertThatThrownBy(() -> service2.writeSomethingWithNewNestedTransactionAndErrors())
                .isExactlyInstanceOf(RuntimeException.class);
        assertThat(service1.readSomething()).isEqualTo(VALUE_1);
    }
}
