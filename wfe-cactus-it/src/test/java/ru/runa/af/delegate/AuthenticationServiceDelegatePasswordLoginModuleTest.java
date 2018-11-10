package ru.runa.af.delegate;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.cactus.ServletTestCase;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.service.AuthenticationService;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;

public class AuthenticationServiceDelegatePasswordLoginModuleTest extends ServletTestCase {

    private static final String PREFIX = AuthenticationServiceDelegatePasswordLoginModuleTest.class.getName();

    private static final String ACTOR1_NAME = "actor1";

    private static final String ACTOR2_NAME = "actor2";

    private static final String ACTOR_VALID_PWD = "validPWD";

    private ServiceTestHelper th;

    private AuthenticationService authenticationService;

    private ExecutorService executorService;

    private Actor validActor;

    protected void setUp() throws Exception {
        th = new ServiceTestHelper(PREFIX);
        authenticationService = th.getAuthenticationService();
        executorService = th.getExecutorService();

        validActor = th.createActorIfNotExist(PREFIX + ACTOR1_NAME, "");
        executorService.setPassword(th.getAdminUser(), validActor, ACTOR_VALID_PWD);

        super.setUp();
    }

    protected void tearDown() throws Exception {
        th.releaseResources();
        th = null;
        authenticationService = null;
        executorService = null;
        validActor = null;
        super.tearDown();
    }

    /*
    private boolean hasActorPrincipal(Subject subject, Actor actor) {
        Set<ActorPrincipal> principals = subject.getPrincipals(ActorPrincipal.class);
        for (ActorPrincipal actorPrincipal : principals) {
            if (actorPrincipal.getActor().equals(actor)) {
                return true;
            }
        }
        return false;
    }
    */

    public void testValidPassword() throws Exception {
        User validSubject = authenticationService.authenticateByLoginPassword(validActor.getName(), ACTOR_VALID_PWD);
        assertEquals("authenticated subject doesn't contains actor principal", validSubject.getActor(), validActor);
    }

    public void testValidPasswordWithAnotherActorWithSamePassword() throws Exception {
        Actor actor2 = th.createActorIfNotExist(PREFIX + ACTOR2_NAME, "");
        executorService.setPassword(th.getAdminUser(), actor2, ACTOR_VALID_PWD);

        User validSubject = authenticationService.authenticateByLoginPassword(validActor.getName(), ACTOR_VALID_PWD);
        assertEquals("authenticated subject doesn't contains actor principal", validSubject.getActor(), validActor);
    }

    public void testLoginFakeActor() throws Exception {
        try {
            authenticationService.authenticateByLoginPassword(th.getFakeActor().getName(), ACTOR_VALID_PWD);
            fail("allowing fake actor");
        } catch (AuthenticationException e) {
            // expected
        }
    }

    public void testInValidPassword() throws Exception {
        try {
            authenticationService.authenticateByLoginPassword(validActor.getName(), ACTOR_VALID_PWD + "Invalid");
            fail("allowing invalid password");
        } catch (AuthenticationException e) {
            // expected
        }
    }

    public void testInValidLogin() throws Exception {
        try {
            authenticationService.authenticateByLoginPassword(validActor.getName() + "Invalid", ACTOR_VALID_PWD);
            fail("allowing invalid login");
        } catch (AuthenticationException e) {
            // expected
        }
    }

    public void testNullPassword() throws Exception {
        try {
            authenticationService.authenticateByLoginPassword(validActor.getName(), null);
            fail("allowing NULL password");
        } catch (IllegalArgumentException e) {
            // TODO
        } catch (AuthenticationException e) {
            fail("TODO trap");
        }
    }
}
