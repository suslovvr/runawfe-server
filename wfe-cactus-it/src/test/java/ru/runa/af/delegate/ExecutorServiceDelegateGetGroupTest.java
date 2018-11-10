package ru.runa.af.delegate;

import com.google.common.collect.Lists;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.cactus.ServletTestCase;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Group;

import java.util.List;
import java.util.Map;

public class ExecutorServiceDelegateGetGroupTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateGetGroupTest.class.getName();

    private Group group;

    private Map<String, Executor> executorsMap;

    protected void setUp() throws Exception {
        executorService = Delegates.getExecutorService();
        th = new ServiceTestHelper(testPrefix);
        th.createDefaultExecutorsMap();
        List<Permission> readPermissions = Lists.newArrayList(Permission.READ);
        executorsMap = th.getDefaultExecutorsMap();

        group = (Group) executorsMap.get(ServiceTestHelper.BASE_GROUP_NAME);
        th.setPermissionsToAuthorizedPerformer(readPermissions, group);
        th.setPermissionsToAuthorizedPerformer(readPermissions, th.getSubGroup());

        th.setPermissionsToAuthorizedPerformer(readPermissions, th.getBaseGroupActor());
        super.setUp();
    }

    public void testGetExecutorByNameByAuthorizedPerformer() throws Exception {
        Group returnedBaseGroup = executorService.getExecutorByName(th.getAuthorizedPerformerUser(), testPrefix + ServiceTestHelper.BASE_GROUP_NAME);
        assertEquals("actor retuned by businessDelegate differes with expected", group, returnedBaseGroup);
        Group returnedSubGroup = executorService.getExecutorByName(th.getAuthorizedPerformerUser(), testPrefix + ServiceTestHelper.SUB_GROUP_NAME);
        Group subGroup = (Group) executorsMap.get(ServiceTestHelper.SUB_GROUP_NAME);
        assertEquals("actor retuned by businessDelegate differes with expected", subGroup, returnedSubGroup);
    }

    public void testGetExecutorByNameByUnauthorizedPerformer() throws Exception {
        try {
            executorService.getExecutorByName(th.getUnauthorizedPerformerUser(), testPrefix + ServiceTestHelper.BASE_GROUP_NAME);
            fail("businessDelegate allow to getExecutorByName()");
        } catch (AuthorizationException e) {
            //That's what we expect
        }
        try {
            executorService.getExecutorByName(th.getUnauthorizedPerformerUser(), testPrefix + ServiceTestHelper.SUB_GROUP_NAME);
            fail("businessDelegate allow to getExecutorByName()");
        } catch (AuthorizationException e) {
            //That's what we expect
        }
    }

    public void testGetUnexistedGroupByAuthorizedPerformer() throws Exception {
        try {
            executorService.getExecutorByName(th.getAuthorizedPerformerUser(), testPrefix + "unexistent group name");
            fail("businessDelegate does not throw Exception to getExecutorByName() in testGetUnexistedGroupByAuthorizedPerformer");
        } catch (ExecutorDoesNotExistException e) {
            //That's what we expect
        }
    }

    public void testGetNullGroupByAuthorizedPerformer() throws Exception {
        try {
            executorService.getExecutorByName(th.getAuthorizedPerformerUser(), null);
            fail("businessDelegate allow to getExecutorByName()with null group.");
        } catch (IllegalArgumentException e) {
            //That's what we expect
        }
    }

    public void testGetExecutorByNameByNullPerformer() throws Exception {
        try {
            executorService.getExecutorByName(null, testPrefix + ServiceTestHelper.BASE_GROUP_NAME);
            fail("businessDelegate allow to getExecutorByName() to performer with null subject.");
        } catch (IllegalArgumentException e) {
            //That's what we expect
        }
    }

    public void testGetActorInsteadOfGroup() throws Exception {
        try {
            Group group = executorService.<Group>getExecutorByName(th.getAuthorizedPerformerUser(), testPrefix + ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
            fail("businessDelegate allow to getExecutorByName() where the actor really is returned.");
        } catch (ClassCastException e) {
            //That's what we expect
        }
    }

    protected void tearDown() throws Exception {
        th.releaseResources();
        executorService = null;
        group = null;
        executorsMap = null;
        super.tearDown();
    }
}
