package ru.runa.wfe.commons.cache.common;

import java.util.concurrent.atomic.AtomicInteger;
import javax.transaction.Transaction;
import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.cache.states.CacheState;
import ru.runa.wfe.commons.cache.states.audit.BeforeTransactionCompleteAudit;
import ru.runa.wfe.commons.cache.states.audit.CacheStateMachineAudit;
import ru.runa.wfe.commons.cache.states.audit.CommitCacheAudit;
import ru.runa.wfe.commons.cache.states.audit.CompleteTransactionAudit;
import ru.runa.wfe.commons.cache.states.audit.GetCacheAudit;
import ru.runa.wfe.commons.cache.states.audit.InitializationErrorAudit;
import ru.runa.wfe.commons.cache.states.audit.OnChangeAudit;
import ru.runa.wfe.commons.cache.states.audit.StageSwitchAudit;

public class TestCacheStateMachineAudit<CacheImpl extends CacheImplementation> implements CacheStateMachineAudit<CacheImpl> {

    private volatile GetCacheAudit<CacheImpl> _getCacheAudit = new TestGetCacheAudit<>();

    private volatile OnChangeAudit<CacheImpl> _onChangeAudit = new TestOnChangeAudit<>();

    private volatile CompleteTransactionAudit<CacheImpl> _completeTransactionAudit =
            new TestCompleteTransactionAudit<>();

    private volatile CommitCacheAudit<CacheImpl> _commitCacheAudit = new TestCommitCacheAudit<>();

    private volatile InitializationErrorAudit<CacheImpl> _initializationErrorAudit =
            new TestInitializationErrorAudit<>();

    private volatile StageSwitchAudit<CacheImpl> _dropCacheAudit = new TestStageSwitchAudit<>();

    private volatile BeforeTransactionCompleteAudit<CacheImpl> _beforeTransactionCompleteAudit =
            new TestBeforeTransactionCompleteAudit<>();

    @Override
    public GetCacheAudit<CacheImpl> auditGetCache() {
        return _getCacheAudit;
    }

    @Override
    public OnChangeAudit<CacheImpl> auditOnChange() {
        return _onChangeAudit;
    }

    @Override
    public CompleteTransactionAudit<CacheImpl> auditCompleteTransaction() {
        return _completeTransactionAudit;
    }

    @Override
    public CommitCacheAudit<CacheImpl> auditCommitCache() {
        return _commitCacheAudit;
    }

    @Override
    public InitializationErrorAudit<CacheImpl> auditInitializationError() {
        return _initializationErrorAudit;
    }

    @Override
    public StageSwitchAudit<CacheImpl> auditDropCache() {
        return _dropCacheAudit;
    }

    @Override
    public BeforeTransactionCompleteAudit<CacheImpl> auditBeforeTransactionComplete() {
        return _beforeTransactionCompleteAudit;
    }

    public void set_getCacheAudit(GetCacheAudit<CacheImpl> _getCacheAudit) {
        this._getCacheAudit = _getCacheAudit;
    }

    public void set_onChangeAudit(OnChangeAudit<CacheImpl> _onChangeAudit) {
        this._onChangeAudit = _onChangeAudit;
    }

    public void set_completeTransactionAudit(CompleteTransactionAudit<CacheImpl> _completeTransactionAudit) {
        this._completeTransactionAudit = _completeTransactionAudit;
    }

    public void set_commitCacheAudit(CommitCacheAudit<CacheImpl> _commitCacheAudit) {
        this._commitCacheAudit = _commitCacheAudit;
    }

    public void set_initializationErrorAudit(InitializationErrorAudit<CacheImpl> _initializationErrorAudit) {
        this._initializationErrorAudit = _initializationErrorAudit;
    }

    public void set_beforeTransactionCompleteAudit(BeforeTransactionCompleteAudit<CacheImpl> _beforeTransactionCompleteAudit) {
        this._beforeTransactionCompleteAudit = _beforeTransactionCompleteAudit;
    }

    public static class TestStageSwitchAudit<CacheImpl extends CacheImplementation> implements StageSwitchAudit<CacheImpl> {

        private final AtomicInteger stayCount = new AtomicInteger();
        private final AtomicInteger switchedCount = new AtomicInteger();
        private final AtomicInteger switchFailedCount = new AtomicInteger();
        private final AtomicInteger fatalErrorCount = new AtomicInteger();

        @Override
        public final void stayStage() {
            stayCount.incrementAndGet();
            _stayStage();
        }

        protected void _stayStage() {
        }

        @Override
        public final void stageSwitched(CacheState<CacheImpl> from, CacheState<CacheImpl> to) {
            switchedCount.incrementAndGet();
            _stageSwitched(from, to);
        }

        protected void _stageSwitched(CacheState<CacheImpl> from, CacheState<CacheImpl> to) {
        }

        @Override
        public final void stageSwitchFailed(CacheState<CacheImpl> from, CacheState<CacheImpl> to) {
            switchFailedCount.incrementAndGet();
            _stageSwitchFailed(from, to);
        }

        protected void _stageSwitchFailed(CacheState<CacheImpl> from, CacheState<CacheImpl> to) {
        }

        @Override
        public final void nextStageFatalError() {
            fatalErrorCount.incrementAndGet();
            throw new RuntimeException("Detected incorrect state behaviour");
        }

        public int getStayCount() {
            return stayCount.get();
        }

        public int getSwitchedCount() {
            return switchedCount.get();
        }

        public int getSwitchFailedCount() {
            return switchFailedCount.get();
        }

        public int getFatalErrorCount() {
            return fatalErrorCount.get();
        }
    }

    public static class TestGetCacheAudit<CacheImpl extends CacheImplementation> extends TestStageSwitchAudit<CacheImpl>
            implements GetCacheAudit<CacheImpl> {
        private final AtomicInteger quickResultCount = new AtomicInteger();
        private final AtomicInteger beforeCreationCount = new AtomicInteger();
        private final AtomicInteger afterCreationCount = new AtomicInteger();

        @Override
        public final void quickResult(Transaction transaction, CacheImpl cache) {
            quickResultCount.incrementAndGet();
            _quickResult(transaction, cache);
        }

        protected void _quickResult(Transaction transaction, CacheImpl cache) {
        }

        @Override
        public final void beforeCreation(Transaction transaction) {
            beforeCreationCount.incrementAndGet();
            _beforeCreation(transaction);
        }

        protected void _beforeCreation(Transaction transaction) {
        }

        @Override
        public final void afterCreation(Transaction transaction, CacheImpl cache) {
            afterCreationCount.incrementAndGet();
            _afterCreation(transaction, cache);
        }

        protected void _afterCreation(Transaction transaction, CacheImpl cache) {
        }

        public int getQuickResultCount() {
            return quickResultCount.get();
        }

        public int getBeforeCreationCount() {
            return beforeCreationCount.get();
        }

        public int getAfterCreationCount() {
            return afterCreationCount.get();
        }
    }

    public static class TestOnChangeAudit<CacheImpl extends CacheImplementation> extends TestStageSwitchAudit<CacheImpl>
            implements OnChangeAudit<CacheImpl> {

        private final AtomicInteger beforeOnChangeCount = new AtomicInteger();
        private final AtomicInteger afterOnChangeCount = new AtomicInteger();

        @Override
        public final void beforeOnChange(Transaction transaction, ChangedObjectParameter changedObject) {
            beforeOnChangeCount.incrementAndGet();
            _beforeOnChange(transaction, changedObject);
        }

        protected void _beforeOnChange(Transaction transaction, ChangedObjectParameter changedObject) {
        }

        @Override
        public final void afterOnChange(Transaction transaction, ChangedObjectParameter changedObject) {
            afterOnChangeCount.incrementAndGet();
            _afterOnChange(transaction, changedObject);
        }

        protected void _afterOnChange(Transaction transaction, ChangedObjectParameter changedObject) {
        }

        public int getBeforeOnChangeCount() {
            return beforeOnChangeCount.get();
        }

        public int getAfterOnChangeCount() {
            return afterOnChangeCount.get();
        }
    }

    public static class TestCompleteTransactionAudit<CacheImpl extends CacheImplementation> extends TestStageSwitchAudit<CacheImpl>
            implements CompleteTransactionAudit<CacheImpl> {

        private final AtomicInteger beforeCompleteTransactionCount = new AtomicInteger();
        private final AtomicInteger afterCompleteTransactionCount = new AtomicInteger();
        private final AtomicInteger allTransactionsCompletedCount = new AtomicInteger();

        @Override
        public final void beforeCompleteTransaction(Transaction transaction) {
            beforeCompleteTransactionCount.incrementAndGet();
            _beforeCompleteTransaction(transaction);
        }

        protected void _beforeCompleteTransaction(Transaction transaction) {
        }

        @Override
        public final void afterCompleteTransaction(Transaction transaction) {
            afterCompleteTransactionCount.incrementAndGet();
            _afterCompleteTransaction(transaction);
        }

        protected void _afterCompleteTransaction(Transaction transaction) {
        }

        @Override
        public final void allTransactionsCompleted(Transaction transaction) {
            allTransactionsCompletedCount.incrementAndGet();
            _allTransactionsCompleted(transaction);
        }

        protected void _allTransactionsCompleted(Transaction transaction) {
        }

        public int getBeforeCompleteTransactionCount() {
            return beforeCompleteTransactionCount.get();
        }

        public int getAfterCompleteTransactionCount() {
            return afterCompleteTransactionCount.get();
        }

        public int getAllTransactionsCompletedCount() {
            return allTransactionsCompletedCount.get();
        }
    }

    public static class TestCommitCacheAudit<CacheImpl extends CacheImplementation> extends TestStageSwitchAudit<CacheImpl>
            implements CommitCacheAudit<CacheImpl> {

        private final AtomicInteger stageIsNotCommitStageCount = new AtomicInteger();
        private final AtomicInteger beforeCommitCount = new AtomicInteger();
        private final AtomicInteger afterCommitCount = new AtomicInteger();

        @Override
        public final void stageIsNotCommitStage(CacheImpl cache) {
            stageIsNotCommitStageCount.incrementAndGet();
            _stageIsNotCommitStage(cache);
        }

        protected void _stageIsNotCommitStage(CacheImpl cache) {
        }

        @Override
        public final void beforeCommit(CacheImpl cache) {
            beforeCommitCount.incrementAndGet();
            _beforeCommit(cache);
        }

        protected void _beforeCommit(CacheImpl cache) {
        }

        @Override
        public final void afterCommit(CacheImpl cache) {
            afterCommitCount.incrementAndGet();
            _afterCommit(cache);
        }

        protected void _afterCommit(CacheImpl cache) {
        }

        public int getStageIsNotCommitStageCount() {
            return stageIsNotCommitStageCount.get();
        }

        public int getBeforeCommitCount() {
            return beforeCommitCount.get();
        }

        public int getAfterCommitCount() {
            return afterCommitCount.get();
        }
    }

    public static class TestInitializationErrorAudit<CacheImpl extends CacheImplementation> extends TestStageSwitchAudit<CacheImpl>
            implements InitializationErrorAudit<CacheImpl> {

        private final AtomicInteger onInitializationErrorCount = new AtomicInteger();

        @Override
        public final void onInitializationError(Throwable e) {
            onInitializationErrorCount.incrementAndGet();
            _onInitializationError(e);
        }

        protected void _onInitializationError(Throwable e) {
        }

        public int getOnInitializationErrorCount() {
            return onInitializationErrorCount.get();
        }
    }

    public static class TestBeforeTransactionCompleteAudit<CacheImpl extends CacheImplementation> extends TestStageSwitchAudit<CacheImpl>
            implements BeforeTransactionCompleteAudit<CacheImpl> {
    }
}
