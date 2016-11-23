package com.fingolfintek.lmdb.tx;

import org.springframework.transaction.support.SmartTransactionObject;

import java.util.Optional;

/**
 * DataSource transaction object, representing a ConnectionHolder.
 * Used as transaction object by LMDBTransactionManager.
 */
class LMDBTransactionObject implements SmartTransactionObject {

    private LMDBTransactionHolder transactionHolder;
    private boolean newConnectionHolder;

    public void setTransactionHolder(LMDBTransactionHolder transactionHolder, boolean newConnectionHolder) {
        this.transactionHolder = transactionHolder;
        this.newConnectionHolder = newConnectionHolder;
    }

    public boolean isNewConnectionHolder() {
        return this.newConnectionHolder;
    }

    public void setRollbackOnly() {
        Optional.ofNullable(transactionHolder)
                .ifPresent(LMDBTransactionHolder::setRollbackOnly);
    }

    @Override
    public boolean isRollbackOnly() {
        return Optional.ofNullable(transactionHolder)
                .map(LMDBTransactionHolder::isRollbackOnly)
                .orElse(Boolean.FALSE);
    }

    @Override
    public void flush() {
    }

    public LMDBTransactionHolder getTransactionHolder() {
        return transactionHolder;
    }

    void removeTransactionHolder() {
        setTransactionHolder(null, false);
    }

    boolean isTransactionActive() {
        return Optional.ofNullable(transactionHolder)
                .map(LMDBTransactionHolder::isTransactionActive)
                .orElse(Boolean.FALSE);
    }
}
