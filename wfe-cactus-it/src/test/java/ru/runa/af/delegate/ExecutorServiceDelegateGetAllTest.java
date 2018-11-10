package ru.runa.af.delegate;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.service.ServiceTestHelper;
import ru.runa.junit.ArrayAssert;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorPermission;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;

import com.google.common.collect.Lists;

public class ExecutorServiceDelegateGetAllTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateGetAllTest.class.getName();

    private Group group;

    private Actor actor;

    private Map<String, Executor> executorsMap;

    @Override
    protected void setUp() throws Exception {
        executorService = Delegates.getExecutorService();
        th = new ServiceTestHelper(testPrefix);
        th.createDefaultExecutorsMap();
        List<Permission> readUpdatePermissions = Lists.newArrayList(Permission.READ, ExecutorPermission.UPDATE);
        executorsMap = th.getDefaultExecutorsMap();

        actor = (Actor) executorsMap.get(ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
        th.setPermissionsToAuthorizedPerformer(readUpdatePermissions, actor);
        group = (Group) executorsMap.get(ServiceTestHelper.BASE_GROUP_NAME);
        th.setPermissionsToAuthorizedPerformer(readUpdatePermissions, group);
        th.setPermissionsToAuthorizedPerformer(readUpdatePermissions, executorsMap.get(ServiceTestHelper.SUB_GROUP_ACTOR_NAME));
        th.setPermissionsToAuthorizedPerformer(readUpdatePermissions, executorsMap.get(ServiceTestHelper.SUB_GROUP_NAME));
        super.setUp();
    }

    final public void testgetExecutorsByAuthorizedPerformer() throws Exception {
        List<? extends Executor> executors = executorService.getExecutors(th.getAuthorizedPerformerUser(), th.getExecutorBatchPresentation());
        LinkedList<Executor> realExecutors = new LinkedList<Executor>(executorsMap.values());
        Actor authorizedPerformerActor = th.getAuthorizedPerformerActor();
        realExecutors.add(authorizedPerformerActor);
        ArrayAssert.assertWeakEqualArrays("businessDelegate.getExecutors() returns wrong executor set", realExecutors, executors);
    }

    public void testgetExecutorsByUnauthorizedPerformer() throws Exception {
        List<? extends Executor> executors = executorService.getExecutors(th.getUnauthorizedPerformerUser(), th.getExecutorBatchPresentation());
        List<Actor> unauthorizedPerformerArray = Lists.newArrayList(th.getUnauthorizedPerformerActor());
        ArrayAssert.assertWeakEqualArrays("businessDelegate.getExecutors() returns wrong executor set", unauthorizedPerformerArray, executors);
    }

    public void testgetExecutorsWithNullSubject() throws Exception {
        try {
            executorService.getExecutors(null, th.getExecutorBatchPresentation());
            fail("businessDelegate.getExecutors() with null subject throws no IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // That's what we expect
        }
    }

    public void testgetExecutorsWithFakeSubject() throws Exception {
        try {
            User fakeUser = th.getFakeUser();
            executorService.getExecutors(fakeUser, th.getExecutorBatchPresentation());
            fail("businessDelegate.getExecutors() with fake subject throws no AuthenticationException");
        } catch (AuthenticationException e) {
            // That's what we expect
        }
    }

    @Override
    protected void tearDown() throws Exception {
        th.releaseResources();
        executorService = null;
        actor = null;
        group = null;
        super.tearDown();
    }
}
