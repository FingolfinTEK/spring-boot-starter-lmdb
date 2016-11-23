package com.fingolfintek.lmdb.tx;

import org.fusesource.lmdbjni.Transaction;
import org.springframework.transaction.support.ResourceHolderSupport;

import java.util.Optional;

public class LMDBTransactionHolder extends ResourceHolderSupport {

    private Transaction transaction;
    private boolean transactionActive = false;

    public LMDBTransactionHolder(Transaction transaction) {
        this.transaction = transaction;
    }

    protected void declareAsActive() {
        this.transactionActive = true;
    }

    protected boolean isTransactionActive() {
        return this.transactionActive;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    @Override
    public void released() {
        super.released();
        if (!isOpen() && this.transaction != null) {
            this.transaction = null;
        }
    }

    @Override
    public void clear() {
        super.clear();
        this.transactionActive = false;
        closeTransaction();
    }

    private void closeTransaction() {
        Optional.ofNullable(transaction)
                .ifPresent(Transaction::close);
    }

}
