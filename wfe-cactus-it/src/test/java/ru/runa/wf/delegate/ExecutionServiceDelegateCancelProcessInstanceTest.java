package ru.runa.wf.delegate;

import java.util.ArrayList;
import java.util.List;

import org.apache.cactus.ServletTestCase;

import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.ProcessPermission;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.collect.Lists;

/**
 * Created on 23.04.2005
 * 
 * @author Gritsenko_S
 */
public class ExecutionServiceDelegateCancelProcessInstanceTest extends ServletTestCase {
    private ExecutionService executionService;

    private WfServiceTestHelper helper = null;

    private WfProcess processInstance = null;

    private BatchPresentation batchPresentation;

    @Override
    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();

        helper.deployValidProcessDefinition();

        executionService.startProcess(helper.getAuthorizedPerformerUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);

        batchPresentation = helper.getProcessInstanceBatchPresentation();

        processInstance = executionService.getProcesses(helper.getAuthorizedPerformerUser(), batchPresentation).get(0);

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition();

        helper.releaseResources();
        executionService = null;
        batchPresentation = null;
        super.tearDown();
    }

    public void testCancelProcessInstanceByAuthorizedSubject() throws Exception {
        helper.setPermissionsToAuthorizedPerformerOnProcessInstance(Lists.newArrayList(ProcessPermission.CANCEL_PROCESS), processInstance);
        executionService.cancelProcess(helper.getAuthorizedPerformerUser(), processInstance.getId());

        List<WfProcess> processInstances = executionService.getProcesses(helper.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("Process not cancelled", 0, processInstances.size());
    }

    public void testCancelProcessInstanceByAuthorizedSubjectWithoutCANCELPermission() throws Exception {
        helper.setPermissionsToAuthorizedPerformerOnProcessInstance(new ArrayList<Permission>(), processInstance);
        try {
            executionService.cancelProcess(helper.getAuthorizedPerformerUser(), processInstance.getId());
            // TODO
            // fail("testCancelProcessInstanceByAuthorizedSubjectWithoutCANCELPermission, no AuthorizationException");
        } catch (AuthorizationException e) {
            fail("TODO trap");
        }
    }

    public void testCancelProcessInstanceByAuthorizedSubjectWithInvalidProcessId() throws Exception {
        helper.setPermissionsToAuthorizedPerformerOnProcessInstance(Lists.newArrayList(ProcessPermission.CANCEL_PROCESS), processInstance);
        try {
            executionService.cancelProcess(helper.getAuthorizedPerformerUser(), -1l);
            // TODO
            // fail("testCancelProcessInstanceByAuthorizedSubjectWithInvalidProcessId, no ProcessInstanceDoesNotExistException");
        } catch (ProcessDoesNotExistException e) {
            fail("TODO trap");
        }
    }

    public void testCancelProcessInstanceByFakeSubject() throws Exception {
        try {
            executionService.cancelProcess(helper.getFakeUser(), processInstance.getId());
            fail("executionDelegate.cancelProcessInstance(helper.getFakeUser(), ..), no AuthenticationException");
        } catch (AuthenticationException e) {
        }
    }

    public void testCancelProcessInstanceByNullUser() throws Exception {
        try {
            executionService.cancelProcess(null, processInstance.getId());
            fail("testCancelProcessInstanceByNullSubject, no IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testCancelProcessInstanceByUnauthorizedSubject() throws Exception {
        try {
            executionService.cancelProcess(helper.getUnauthorizedPerformerUser(), processInstance.getId());
            List<WfProcess> processInstances = executionService.getProcesses(helper.getAuthorizedPerformerUser(), batchPresentation);
            assertEquals("Process was cancelled by unauthorized subject", 1, processInstances.size());
        } catch (AuthorizationException e) {
        }
    }
}
