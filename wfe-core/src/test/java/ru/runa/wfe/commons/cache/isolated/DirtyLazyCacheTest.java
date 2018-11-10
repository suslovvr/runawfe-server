package ru.runa.wfe.commons.cache.isolated;

import lombok.val;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.runa.wfe.commons.ManualResetEvent;
import ru.runa.wfe.commons.cache.Change;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.cache.common.TestCacheIface;
import ru.runa.wfe.commons.cache.common.TestCacheStateMachineAudit;
import ru.runa.wfe.commons.cache.common.TestLazyCache;
import ru.runa.wfe.commons.cache.common.TestLazyCacheCtrl;
import ru.runa.wfe.commons.cache.common.TestLazyCacheFactoryCallback;
import ru.runa.wfe.commons.cache.common.TestLazyCacheStub;
import ru.runa.wfe.commons.cache.common.TestTransaction;
import ru.runa.wfe.commons.cache.states.CacheState;

public class DirtyLazyCacheTest {

    final Class<? extends TestCacheIface> cacheClass = TestLazyCache.class;
    final Class<? extends TestCacheIface> proxyClass = TestLazyCacheStub.class;

    @Test()
    public void simpleGetCacheTest() {
        val initializationCompleteEvent = new ManualResetEvent();
        val ctrl = new TestLazyCacheCtrl(new TestLazyCacheFactoryCallback(), true);
        ctrl.getAudit().set_commitCacheAudit(new TestCacheStateMachineAudit.TestCommitCacheAudit<TestCacheIface>() {
            @Override
            protected void _stageSwitched(CacheState<TestCacheIface> from, CacheState<TestCacheIface> to) {
                initializationCompleteEvent.setEvent();
            }
        });
        ctrl.getCache(false);
        initializationCompleteEvent.tryWaitEvent();
        TestCacheIface cacheInstance = ctrl.getCurrentCacheInstance();
        Assert.assertNotNull(cacheInstance);
        Assert.assertSame(ctrl.getCache(false), cacheInstance);
        Assert.assertSame(ctrl.getCacheIfNotLocked(false), cacheInstance);
        Assert.assertSame(ctrl.getCache(true), cacheInstance);
        Assert.assertSame(ctrl.getCacheIfNotLocked(true), cacheInstance);
        ctrl.onChanged(new ChangedObjectParameter(1L, Change.DELETE, null, null, null));
        Assert.assertSame(ctrl.getCacheIfNotLocked(false), null);
        Assert.assertSame(ctrl.getCacheIfNotLocked(true), null);
        Assert.assertSame(ctrl.getCache(false).getClass(), proxyClass);
        Assert.assertSame(ctrl.getCache(true).getClass(), proxyClass);
        Assert.assertSame(ctrl.getCacheIfNotLocked(true).getClass(), proxyClass);
        Assert.assertSame(ctrl.getCacheIfNotLocked(new TestTransaction(), false), cacheInstance);
        Assert.assertSame(ctrl.getCache(new TestTransaction(), false), cacheInstance);
    }
}
