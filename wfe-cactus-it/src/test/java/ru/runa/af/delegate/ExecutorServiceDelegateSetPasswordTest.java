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
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.ExecutorPermission;
import ru.runa.wfe.user.Group;

import java.util.List;

/**
 * Created on 27.08.2004 
 */
public class ExecutorServiceDelegateSetPasswordTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static final String testPrefix = ExecutorServiceDelegateSetPasswordTest.class.getName();

    private static final String NEW_PASSWD = "new passwd";

    private Actor actor;

    private Group group;

    protected void setUp() throws Exception {
        executorService = Delegates.getExecutorService();
        th = new ServiceTestHelper(testPrefix);
        th.createDefaultExecutorsMap();
        List<Permission> updatePermissions = Lists.newArrayList(ExecutorPermission.UPDATE);

        actor = th.getBaseGroupActor();
        th.setPermissionsToAuthorizedPerformer(updatePermissions, actor);
        group = th.getBaseGroup();
        th.setPermissionsToAuthorizedPerformer(updatePermissions, group);
        super.setUp();
    }

    protected void tearDown() throws Exception {
        th.releaseResources();
        executorService = null;
        actor = null;
        group = null;
        super.tearDown();
    }

    public void testSetPasswordByAuthorizedPerformer() throws Exception {
        executorService.setPassword(th.getAuthorizedPerformerUser(), actor, NEW_PASSWD);

        assertTrue("Password is not correct.", th.isPasswordCorrect(actor.getName(), NEW_PASSWD));
    }

    public void testSetPasswordByUnauthorizedPerformer() throws Exception {

        try {
            executorService.setPassword(th.getUnauthorizedPerformerUser(), actor, NEW_PASSWD);
            fail("Password was changed without permission.");
        } catch (AuthorizationException e) {
            // This is what must happen
        }
        assertFalse("Password was changed without permission.", th.isPasswordCorrect(actor.getName(), NEW_PASSWD));
    }

    public void testSetPasswordToNullExecutor() throws Exception {
        try {
            executorService.setPassword(th.getAuthorizedPerformerUser(), null, NEW_PASSWD);
            assertTrue("IllegalArgumentException was not thrown on setting password to null executor", false);
        } catch (IllegalArgumentException e) {
            //that's what we expect to see
        }
    }

    public void testSetPasswordToFakeActor() throws Exception {
        try {
            executorService.setPassword(th.getAuthorizedPerformerUser(), th.getFakeActor(), NEW_PASSWD);
            assertTrue("ExecutorOutOfDateException was not thrown on setPassword fakeActor argument.", false);
        } catch (AuthorizationException e) {
            //that's what we expect to see
        }
    }

}
