package ru.runa.wfe.commons.cache.states.nonruntime;

import javax.transaction.Transaction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.cache.sm.CacheStateMachineContext;
import ru.runa.wfe.commons.cache.states.CacheState;
import ru.runa.wfe.commons.cache.states.DirtyTransactions;
import ru.runa.wfe.commons.cache.states.StateCommandResult;
import ru.runa.wfe.commons.cache.states.StateCommandResultWithCache;
import ru.runa.wfe.commons.cache.states.StateCommandResultWithData;

/**
 * Cache lifetime state machine. Current state is fully operational cache (cache is initialized).
 */
public class CompletedCacheState<CacheImpl extends CacheImplementation> implements CacheState<CacheImpl> {

    /**
     * Logging support.
     */
    private static Log log = LogFactory.getLog(EmptyCacheState.class);

    /**
     * Current cache instance.
     */
    private final CacheImpl cache;

    public CompletedCacheState(CacheImpl cache) {
        this.cache = cache;
    }

    @Override
    public boolean isDirtyTransactionExists() {
        return false;
    }

    @Override
    public boolean isDirtyTransaction(Transaction transaction) {
        return false;
    }

    @Override
    public CacheImpl getCacheQuickNoBuild(Transaction transaction) {
        return cache;
    }

    @Override
    public StateCommandResultWithCache<CacheImpl> getCache(CacheStateMachineContext<CacheImpl> context, Transaction transaction) {
        return StateCommandResultWithCache.createNoStateSwitch(cache);
    }

    @Override
    public StateCommandResultWithCache<CacheImpl> getCacheIfNotLocked(CacheStateMachineContext<CacheImpl> context, Transaction transaction) {
        return StateCommandResultWithCache.createNoStateSwitch(cache);
    }

    @Override
    public StateCommandResult<CacheImpl> onChange(
            CacheStateMachineContext<CacheImpl> context, Transaction transaction, ChangedObjectParameter changedObject
    ) {
        cache.onChange(changedObject);
        DirtyTransactions<CacheImpl> dirtyTransaction = DirtyTransactions.createOneDirtyTransaction(transaction, cache);
        return StateCommandResult.create(context.getStateFactory().createDirtyState(cache, dirtyTransaction));
    }

    @Override
    public StateCommandResult<CacheImpl> beforeTransactionComplete(CacheStateMachineContext<CacheImpl> context, Transaction transaction) {
        return StateCommandResult.createNoStateSwitch();
    }

    @Override
    public StateCommandResultWithData<CacheImpl, Boolean> completeTransaction(CacheStateMachineContext<CacheImpl> context, Transaction transaction) {
        log.error("completeTransaction must not be called on " + this);
        return StateCommandResultWithData.create(context.getStateFactory().createEmptyState(cache), true);
    }

    @Override
    public StateCommandResult<CacheImpl> commitCache(CacheStateMachineContext<CacheImpl> context, CacheImpl cache) {
        log.error("commitCache must not be called on " + this);
        return StateCommandResult.createNoStateSwitch();
    }

    @Override
    public void discard() {
    }

    @Override
    public void accept(CacheStateMachineContext<CacheImpl> context) {
    }

    @Override
    public StateCommandResult<CacheImpl> dropCache(CacheStateMachineContext<CacheImpl> context) {
        return StateCommandResult.create(context.getStateFactory().createEmptyState(null));
    }
}
