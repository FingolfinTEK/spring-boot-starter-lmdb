package com.fingolfintek.lmdb;

import com.fingolfintek.lmdb.tx.LMDBTransactionManager;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.fusesource.lmdbjni.Database;
import org.fusesource.lmdbjni.Entry;
import org.fusesource.lmdbjni.EntryIterator;
import org.fusesource.lmdbjni.Transaction;
import org.springframework.transaction.NoTransactionException;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class LMDBTemplate {

    private final LMDBTransactionManager txManager;
    private final LMDBCodec codec;
    private final KeyGenerator generator;

    public LMDBTemplate(
            LMDBTransactionManager txManager, LMDBCodec codec, KeyGenerator generator) {
        this.txManager = txManager;
        this.codec = codec;
        this.generator = generator;
    }

    public <T> T get(Database db, Key key) {
        return get(db, key.asBytes());
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Database db, byte[] key) {
        byte[] bytes = getRaw(db, key);
        return (T) codec.unmarshal(bytes);
    }

    private byte[] getRaw(Database db, byte[] key) {
        return readUnderTransaction(tx -> db.get(tx, key));
    }

    private <T> T readUnderTransaction(Function<Transaction, T> reader) {
        return txManager.getCurrentlyActiveTransaction()
                .map(reader)
                .getOrElseThrow(noTxError("Read failed"));
    }

    private Supplier<NoTransactionException> noTxError(String msg) {
        return () -> new NoTransactionException(msg);
    }

    public <T> T get(Database db, Key key, Class<T> returnType) {
        return get(db, key.asBytes(), returnType);
    }

    public <T> T get(Database db, byte[] key, Class<T> returnType) {
        byte[] bytes = getRaw(db, key);
        return codec.unmarshal(bytes, returnType);
    }

    public void write(Database db, Object value) {
        byte[] key = generator.generateUniqueRawKey();
        write(db, key, value);
    }

    public void write(Database db, Key key, Object value) {
        write(db, key.asBytes(), value);
    }

    public void write(Database db, byte[] key, Object value) {
        byte[] marshal = codec.marshal(value);
        writeUnderTransaction(tx -> db.put(tx, key, marshal));
    }

    private void writeUnderTransaction(Consumer<Transaction> writer) {
        Transaction tx = txManager.getCurrentlyActiveTransaction()
                .getOrElseThrow(noTxError("Write failed"));

        writer.accept(tx);
    }

    public void delete(Database db, Key... keys) {
        writeUnderTransaction(tx -> Stream.of(keys).forEach(
                key -> db.delete(tx, key.asBytes())));
    }

    public <T> Stream<Tuple2<Key, T>> iterate(Database db, Class<T> entryType) {
        return readUnderTransaction(tx -> {
            EntryIterator iterator = db.iterate(tx);
            return LambdaUtils.create(iterator)
                    .map(toKeyAndValueOfType(entryType));
        });
    }

    public <T> List<Tuple2<Key, T>> readAllEntries(Database db, Class<T> entryType) {
        return iterate(db, entryType).toJavaList();
    }

    private <T> Function<Entry, Tuple2<Key, T>> toKeyAndValueOfType(Class<T> entryType) {
        return e -> Tuple.of(Key.from(e.getKey()), codec.unmarshal(e.getValue(), entryType));
    }


    public <T> Stream<T> iterateValues(Database db, Class<T> entryType) {
        return iterate(db, entryType).map(Tuple2::_2);
    }

}
