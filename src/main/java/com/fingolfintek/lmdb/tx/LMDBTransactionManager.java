package com.fingolfintek.lmdb.tx;

import javaslang.control.Option;
import javaslang.control.Try;
import org.fusesource.lmdbjni.Env;
import org.fusesource.lmdbjni.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;
import java.util.function.Consumer;

import static org.springframework.transaction.support.TransactionSynchronizationManager.bindResource;
import static org.springframework.transaction.support.TransactionSynchronizationManager.unbindResource;

public class LMDBTransactionManager extends AbstractPlatformTransactionManager {

    private static final long serialVersionUID = 0;
    
    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private final transient Env environment;

    public LMDBTransactionManager(Env environment) {
        this.environment = environment;
    }

    @Override
    protected Object doGetTransaction() {
        LMDBTransactionHolder txHolder = getCurrentlyActiveTransactionHolder();
        LMDBTransactionObject txObject = new LMDBTransactionObject();
        txObject.setTransactionHolder(txHolder, false);
        return txObject;
    }

    private LMDBTransactionHolder getCurrentlyActiveTransactionHolder() {
        return (LMDBTransactionHolder) TransactionSynchronizationManager.getResource(environment);
    }

    public Option<Transaction> getCurrentlyActiveTransaction() {
        return Option.of(getCurrentlyActiveTransactionHolder())
                .map(LMDBTransactionHolder::getTransaction);
    }

    @Override
    protected boolean isExistingTransaction(Object transaction) {
        LMDBTransactionObject txObject = (LMDBTransactionObject) transaction;
        return txObject.isTransactionActive();
    }

    /**
     * This implementation sets the isolation level but ignores the timeout.
     */
    @Override
    protected void doBegin(Object object, TransactionDefinition definition) {
        LMDBTransactionObject txObject = (LMDBTransactionObject) object;
        try {
            if (shouldCreateNewTransactionFor(txObject)) {
                LMDBTransactionHolder holder = createTransactionHolderFor(definition);
                txObject.setTransactionHolder(holder, true);
            }

            txObject.getTransactionHolder().setSynchronizedWithTransaction(true);
            txObject.getTransactionHolder().declareAsActive();

            bindTransactionToCurrentThreadIfNew(txObject);
        } catch (Throwable ex) {
            unbindTransactionFromCurrentThreadIfNew(txObject);
            throw new CannotCreateTransactionException("Could not open LMDB Connection for transaction", ex);
        }
    }

    private LMDBTransactionHolder createTransactionHolderFor(TransactionDefinition definition) {
        Transaction newTx = createTransactionFor(definition);
        return new LMDBTransactionHolder(newTx);
    }

    private Transaction createTransactionFor(TransactionDefinition definition) {
        Transaction tx = Optional.of(definition)
                .filter(TransactionDefinition::isReadOnly)
                .map(td -> environment.createReadTransaction())
                .orElseGet(environment::createWriteTransaction);
        logger.debug("Acquired Transaction [{}] for LMDB transaction", tx);
        return tx;
    }

    private void bindTransactionToCurrentThreadIfNew(LMDBTransactionObject txObject) {
        onlyNewTransactionObject(txObject)
                .map(LMDBTransactionObject::getTransactionHolder)
                .ifPresent(holder -> bindResource(environment, holder));
    }

    private Optional<LMDBTransactionObject> onlyNewTransactionObject(LMDBTransactionObject txObject) {
        return Optional.ofNullable(txObject)
                .filter(LMDBTransactionObject::isNewConnectionHolder);
    }

    private void unbindTransactionFromCurrentThreadIfNew(LMDBTransactionObject txObject) {
        onlyNewTransactionObject(txObject)
                .ifPresent(LMDBTransactionObject::removeTransactionHolder);
    }

    private boolean shouldCreateNewTransactionFor(LMDBTransactionObject txObject) {
        return Optional.ofNullable(txObject.getTransactionHolder())
                .map(LMDBTransactionHolder::isSynchronizedWithTransaction)
                .orElse(Boolean.TRUE);
    }

    @Override
    protected Object doSuspend(Object transaction) {
        LMDBTransactionObject txObject = (LMDBTransactionObject) transaction;
        txObject.removeTransactionHolder();
        return unbindResource(environment);
    }

    @Override
    protected void doResume(Object transaction, Object suspendedResources) {
        bindResource(environment, suspendedResources);
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) {
        doWithTransaction(status, tx -> {
            logger.debug("Committing LMDB transaction on Connection [{}]", tx);
            tx.commit();
        }, "Could not commit LMDB transaction");
    }

    private Try<Void> doWithTransaction(
            DefaultTransactionStatus status, Consumer<Transaction> consumer, String errorMessage) {
        LMDBTransactionObject txObject = (LMDBTransactionObject) status.getTransaction();
        Transaction tx = txObject.getTransactionHolder().getTransaction();
        return Try.run(() -> consumer.accept(tx))
                .onFailure(ex -> {
                    throw new TransactionSystemException(errorMessage, ex);
                });
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) {
        doWithTransaction(status, tx -> {
            logger.debug("Rolling back LMDB transaction on Connection [{}]", tx);
            tx.abort();
        }, "Could not roll back LMDB transaction");
    }

    @Override
    protected void doSetRollbackOnly(DefaultTransactionStatus status) {
        LMDBTransactionObject txObject = (LMDBTransactionObject) status.getTransaction();
        logger.debug("Setting LMDB transaction [{}] rollback-only", txObject.getTransactionHolder());
        txObject.setRollbackOnly();
    }

    @Override
    protected void doCleanupAfterCompletion(Object transaction) {
        LMDBTransactionObject txObject = (LMDBTransactionObject) transaction;
        txObject.getTransactionHolder().clear();
        unbindResource(environment);
    }

}
