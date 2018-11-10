package ru.runa.wfe.execution.logic;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.transaction.UserTransaction;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.ConfigurationException;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.BaseProcessLog;
import ru.runa.wfe.audit.CurrentAdminActionLog;
import ru.runa.wfe.audit.CurrentProcessActivateLog;
import ru.runa.wfe.audit.CurrentProcessCancelLog;
import ru.runa.wfe.audit.CurrentProcessEndLog;
import ru.runa.wfe.audit.CurrentProcessSuspendLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.Errors;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.TransactionListeners;
import ru.runa.wfe.commons.TransactionalExecutor;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.commons.cache.CacheResetTransactionListener;
import ru.runa.wfe.commons.error.ProcessError;
import ru.runa.wfe.commons.error.ProcessErrorType;
import ru.runa.wfe.commons.logic.WfCommonLogic;
import ru.runa.wfe.definition.DefinitionVariableProvider;
import ru.runa.wfe.definition.ProcessDefinitionVersion;
import ru.runa.wfe.definition.ProcessDefinitionWithVersion;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.CurrentNodeProcess;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.CurrentProcessClassPresentation;
import ru.runa.wfe.execution.CurrentSwimlane;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.NodeProcess;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.ProcessFactory;
import ru.runa.wfe.execution.ProcessFilter;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.async.NodeAsyncExecutor;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.execution.dto.WfSwimlane;
import ru.runa.wfe.execution.dto.WfToken;
import ru.runa.wfe.extension.AssignmentHandler;
import ru.runa.wfe.extension.ProcessEndHandler;
import ru.runa.wfe.extension.assign.AssignmentHelper;
import ru.runa.wfe.graph.DrawProperties;
import ru.runa.wfe.graph.history.GraphHistoryBuilder;
import ru.runa.wfe.graph.image.GraphImageBuilder;
import ru.runa.wfe.graph.view.NodeGraphElement;
import ru.runa.wfe.graph.view.NodeGraphElementBuilder;
import ru.runa.wfe.graph.view.ProcessGraphInfoVisitor;
import ru.runa.wfe.job.Job;
import ru.runa.wfe.job.dao.JobDao;
import ru.runa.wfe.job.dto.WfJob;
import ru.runa.wfe.lang.AsyncCompletionMode;
import ru.runa.wfe.lang.BaseTaskNode;
import ru.runa.wfe.lang.BoundaryEvent;
import ru.runa.wfe.lang.Delegation;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.lang.SubprocessNode;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.lang.Synchronizable;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.filter.StringFilterCriteria;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskCompletionInfo;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.TemporaryGroup;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.dao.ExecutorDao;
import ru.runa.wfe.user.logic.ExecutorLogic;
import ru.runa.wfe.var.MapDelegableVariableProvider;
import ru.runa.wfe.var.Variable;
import ru.runa.wfe.var.VariableProvider;

/**
 * Process execution logic.
 *
 * @author Dofs
 * @since 2.0
 */
@Component
public class ExecutionLogic extends WfCommonLogic {
    private static final SecuredObjectType[] PROCESS_EXECUTION_CLASSES = { SecuredObjectType.PROCESS };
    @Autowired
    private ProcessFactory processFactory;
    @Autowired
    private ExecutorLogic executorLogic;
    @Autowired
    private NodeAsyncExecutor nodeAsyncExecutor;

    public void cancelProcess(User user, Long processId) throws ProcessDoesNotExistException {
        ProcessFilter filter = new ProcessFilter();
        Preconditions.checkArgument(processId != null);
        filter.setId(processId);
        cancelProcesses(user, filter);
    }

    public int getProcessesCount(User user, BatchPresentation batchPresentation) {
        return getPersistentObjectCount(user, batchPresentation, Permission.LIST, PROCESS_EXECUTION_CLASSES);
    }

    public List<WfProcess> getProcesses(User user, BatchPresentation batchPresentation) {
        List<? extends Process> pp = getPersistentObjects(user, batchPresentation, Permission.LIST, PROCESS_EXECUTION_CLASSES, true);
        return toWfProcesses(pp, batchPresentation.getDynamicFieldsToDisplay(true));
    }

    public void deleteProcesses(User user, final ProcessFilter filter) {
        List<CurrentProcess> processes = getCurrentProcessesInternal(user, filter);
        // TODO add ProcessPermission.DELETE_PROCESS
        processes = filterSecuredObject(user, processes, Permission.CANCEL);
        for (CurrentProcess process : processes) {
            deleteProcess(user, process);
        }
    }

    public void cancelProcesses(User user, final ProcessFilter filter) {
        List<CurrentProcess> processes = getCurrentProcessesInternal(user, filter);
        processes = filterSecuredObject(user, processes, Permission.CANCEL);
        for (CurrentProcess process : processes) {
            ParsedProcessDefinition parsedProcessDefinition = getDefinition(process);
            ExecutionContext executionContext = new ExecutionContext(parsedProcessDefinition, process);
            endProcess(process, executionContext, user.getActor());
            log.info(process + " was cancelled by " + user);
        }
    }

    public void failProcessExecution(UserTransaction transaction, Long tokenId, Throwable throwable) {
        new TransactionalExecutor(transaction) {

            @Override
            protected void doExecuteInTransaction() {
                CurrentToken token = ApplicationContextFactory.getCurrentTokenDao().getNotNull(tokenId);
                boolean stateChanged = failToken(token, Throwables.getRootCause(throwable));
                if (stateChanged) {
                    token.getProcess().setExecutionStatus(ExecutionStatus.FAILED);
                    ProcessError processError = new ProcessError(ProcessErrorType.execution, token.getProcess().getId(), token.getNodeId());
                    processError.setThrowable(throwable);
                    Errors.sendEmailNotification(processError);
                }
            }
        }.executeInTransaction(true);
    }

    public boolean failToken(CurrentToken token, Throwable throwable) {
        boolean stateChanged = token.getExecutionStatus() != ExecutionStatus.FAILED;
        token.setExecutionStatus(ExecutionStatus.FAILED);
        token.setErrorDate(new Date());
        // safe for unicode
        String errorMessage = Utils.getCuttedString(throwable.toString(), 1024 / 2);
        stateChanged |= !Objects.equal(errorMessage, token.getErrorMessage());
        token.setErrorMessage(errorMessage);
        return stateChanged;
    }

    /**
     * Ends specified process and all the tokens in it.
     *
     * @param canceller
     *            actor who cancels process (if any), can be <code>null</code>
     */
    public void endProcess(CurrentProcess process, ExecutionContext executionContext, Actor canceller) {
        if (process.hasEnded()) {
            log.debug(this + " already ended");
            return;
        }
        log.info("Ending " + this + " by " + canceller);
        Errors.removeProcessErrors(process.getId());
        TaskCompletionInfo taskCompletionInfo = TaskCompletionInfo.createForProcessEnd(process.getId());
        // end the main path of execution
        endToken(process.getRootToken(), executionContext.getParsedProcessDefinition(), canceller, taskCompletionInfo, true);
        // mark this process as ended
        process.setEndDate(new Date());
        process.setExecutionStatus(ExecutionStatus.ENDED);
        // check if this process was started as a subprocess of a super
        // process
        CurrentNodeProcess parentNodeProcess = executionContext.getCurrentParentNodeProcess();
        if (parentNodeProcess != null && !parentNodeProcess.getParentToken().hasEnded()) {
            ProcessDefinitionLoader processDefinitionLoader = ApplicationContextFactory.getProcessDefinitionLoader();
            ParsedProcessDefinition parentProcessDefinition = processDefinitionLoader.getDefinition(parentNodeProcess.getProcess());
            Node node = parentProcessDefinition.getNodeNotNull(parentNodeProcess.getNodeId());
            Synchronizable synchronizable = (Synchronizable) node;
            if (!synchronizable.isAsync()) {
                log.info("Signalling to parent " + parentNodeProcess.getProcess());
                endSubprocessSignalToken(parentNodeProcess.getParentToken(), executionContext);
            }
        }

        // make sure all the timers for this process are canceled
        // after the process end updates are posted to the database
        JobDao jobDao = ApplicationContextFactory.getJobDao();
        jobDao.deleteByProcess(process);
        if (canceller != null) {
            executionContext.addLog(new CurrentProcessCancelLog(canceller));
        } else {
            executionContext.addLog(new CurrentProcessEndLog());
        }
        // flush just created tasks
        ApplicationContextFactory.getTaskDao().flushPendingChanges();
        boolean activeSuperProcessExists = parentNodeProcess != null && !parentNodeProcess.getProcess().hasEnded();
        for (Task task : ApplicationContextFactory.getTaskDao().findByProcess(process)) {
            BaseTaskNode taskNode = (BaseTaskNode) executionContext.getParsedProcessDefinition().getNodeNotNull(task.getNodeId());
            if (taskNode.isAsync()) {
                switch (taskNode.getCompletionMode()) {
                    case NEVER:
                        continue;
                    case ON_MAIN_PROCESS_END:
                        if (activeSuperProcessExists) {
                            continue;
                        }
                    case ON_PROCESS_END:
                }
            }
            task.end(executionContext, taskNode, taskCompletionInfo);
        }
        if (parentNodeProcess == null) {
            log.debug("Removing async tasks and subprocesses ON_MAIN_PROCESS_END");
            endSubprocessAndTasksOnMainProcessEndRecursively(process, executionContext, canceller);
        }
        for (CurrentSwimlane swimlane : ApplicationContextFactory.getCurrentSwimlaneDao().findByProcess(process)) {
            if (swimlane.getExecutor() instanceof TemporaryGroup) {
                swimlane.setExecutor(null);
            }
        }
        for (CurrentProcess subProcess : executionContext.getCurrentSubprocessesRecursively()) {
            for (CurrentSwimlane swimlane : ApplicationContextFactory.getCurrentSwimlaneDao().findByProcess(subProcess)) {
                if (swimlane.getExecutor() instanceof TemporaryGroup) {
                    swimlane.setExecutor(null);
                }
            }
        }
        for (String processEndHandlerClassName : SystemProperties.getProcessEndHandlers()) {
            try {
                ProcessEndHandler handler = ClassLoaderUtil.instantiate(processEndHandlerClassName);
                handler.execute(executionContext);
            } catch (Throwable th) {
                Throwables.propagate(th);
            }
        }
        if (SystemProperties.deleteTemporaryGroupsOnProcessEnd()) {
            ExecutorDao executorDao = ApplicationContextFactory.getExecutorDao();
            List<TemporaryGroup> groups = executorDao.getTemporaryGroups(process.getId());
            for (TemporaryGroup temporaryGroup : groups) {
                if (ApplicationContextFactory.getCurrentProcessDao().getDependentProcessIds(temporaryGroup, 1).isEmpty()) {
                    log.debug("Cleaning " + temporaryGroup);
                    executorDao.remove(temporaryGroup);
                } else {
                    log.debug("Group " + temporaryGroup + " deletion postponed");
                }
            }
        }
    }

    private void endSubprocessAndTasksOnMainProcessEndRecursively(CurrentProcess process, ExecutionContext executionContext, Actor canceller) {
        List<CurrentProcess> subprocesses = executionContext.getCurrentSubprocesses();
        if (subprocesses.size() > 0) {
            ProcessDefinitionLoader processDefinitionLoader = ApplicationContextFactory.getProcessDefinitionLoader();
            for (CurrentProcess subProcess : subprocesses) {
                ParsedProcessDefinition subProcessDefinition = processDefinitionLoader.getDefinition(subProcess);
                ExecutionContext subExecutionContext = new ExecutionContext(subProcessDefinition, subProcess);

                endSubprocessAndTasksOnMainProcessEndRecursively(process, subExecutionContext, canceller);

                for (Task task : ApplicationContextFactory.getTaskDao().findByProcess(subProcess)) {
                    BaseTaskNode taskNode = (BaseTaskNode) subProcessDefinition.getNodeNotNull(task.getNodeId());
                    if (taskNode.isAsync()) {
                        switch (taskNode.getCompletionMode()) {
                            case NEVER:
                            case ON_PROCESS_END:
                                continue;
                            case ON_MAIN_PROCESS_END:
                                task.end(subExecutionContext, taskNode, TaskCompletionInfo.createForProcessEnd(process.getId()));
                        }
                    }
                }

                if (!subProcess.hasEnded()) {
                    CurrentNodeProcess nodeProcess = ApplicationContextFactory.getCurrentNodeProcessDao().findBySubProcessId(subProcess.getId());
                    SubprocessNode subprocessNode = (SubprocessNode) executionContext.getParsedProcessDefinition().getNodeNotNull(nodeProcess.getNodeId());
                    if (subprocessNode.getCompletionMode() == AsyncCompletionMode.ON_MAIN_PROCESS_END) {
                        endProcess(subProcess, subExecutionContext, canceller);
                    }
                }
            }
        }
    }

    private void endSubprocessSignalToken(CurrentToken token, ExecutionContext subExecutionContext) {
        if (!token.hasEnded()) {
            if (token.getNodeType() != NodeType.SUBPROCESS && token.getNodeType() != NodeType.MULTI_SUBPROCESS) {
                throw new InternalApplicationException(
                        "Unexpected token node " + token.getNodeId() + " of type " + token.getNodeType() + " on subprocess end"
                );
            }
            CurrentNodeProcess parentNodeProcess = subExecutionContext.getCurrentParentNodeProcess();
            Long parentDefinitionVersionId = parentNodeProcess.getProcess().getDefinitionVersion().getId();
            ParsedProcessDefinition superDefinition = ApplicationContextFactory.getProcessDefinitionLoader().getDefinition(parentDefinitionVersionId);
            token.getNodeNotNull(superDefinition).leave(subExecutionContext, null);
        }
    }

    /**
     * Ends specified token and all of its children (if recursive).
     *
     * @param canceller
     *            actor who cancels process (if any), can be <code>null</code>
     */
    public void endToken(
            CurrentToken token, ParsedProcessDefinition processDefinition, Actor canceller, TaskCompletionInfo taskCompletionInfo, boolean recursive
    ) {
        ProcessDefinitionLoader processDefinitionLoader = ApplicationContextFactory.getProcessDefinitionLoader();
        ExecutionLogic executionLogic = ApplicationContextFactory.getExecutionLogic();

        ExecutionContext executionContext = new ExecutionContext(processDefinition, token);
        if (token.hasEnded()) {
            log.debug(this + " already ended");
            return;
        }
        log.info("Ending " + this + " by " + canceller);
        token.setEndDate(new Date());
        token.setExecutionStatus(ExecutionStatus.ENDED);
        Node node = processDefinition.getNode(token.getNodeId());
        if (node instanceof SubprocessNode) {
            for (CurrentProcess subProcess : executionContext.getCurrentTokenSubprocesses()) {
                ParsedProcessDefinition subProcessDefinition = processDefinitionLoader.getDefinition(subProcess);
                executionLogic.endProcess(subProcess, new ExecutionContext(subProcessDefinition, subProcess), canceller);
            }
        } else if (node instanceof BaseTaskNode) {
            ((BaseTaskNode) node).endTokenTasks(executionContext, taskCompletionInfo);
        } else if (node instanceof BoundaryEvent) {
            log.info("Cancelling " + node + " with " + this);
            ((BoundaryEvent) node).cancelBoundaryEvent(token);
        } else if (node == null) {
            log.warn("Node is null");
        }
        if (recursive) {
            for (CurrentToken child : token.getChildren()) {
                executionLogic.endToken(child, executionContext.getParsedProcessDefinition(), canceller, taskCompletionInfo, true);
            }
        }
    }

    public WfProcess getProcess(User user, Long id) throws ProcessDoesNotExistException {
        Process process = processDao.getNotNull(id);
        permissionDao.checkAllowed(user, Permission.LIST, process);
        return new WfProcess(process);
    }

    public WfProcess getParentProcess(User user, Long processId) throws ProcessDoesNotExistException {
        NodeProcess nodeProcess = nodeProcessDao.findBySubProcessId(processId);
        if (nodeProcess == null) {
            return null;
        }
        Process parentProcess = nodeProcess.getProcess();
        permissionDao.checkAllowed(user, Permission.LIST, parentProcess);  // TODO Should also check permission on subprocess?
        return new WfProcess(parentProcess);
    }

    public List<WfProcess> getSubprocesses(User user, Long processId, boolean recursive) throws ProcessDoesNotExistException {
        Process process = processDao.getNotNull(processId);
        List<? extends Process> subprocesses = recursive
                ? nodeProcessDao.getSubprocessesRecursive(process)
                : nodeProcessDao.getSubprocesses(process);
        subprocesses = filterSecuredObject(user, subprocesses, Permission.LIST);  // TODO Should also check permission on parent process?
        return toWfProcesses(subprocesses, null);
    }

    public List<WfJob> getJobs(User user, Long processId, boolean recursive) throws ProcessDoesNotExistException {
        Process p = processDao.getNotNull(processId);
        permissionDao.checkAllowed(user, Permission.LIST, p);
        if (p.isArchive()) {
            return Collections.emptyList();
        }
        val cp = (CurrentProcess) p;
        List<Job> jobs = jobDao.findByProcess(cp);
        if (recursive) {
            List<CurrentProcess> subprocesses = currentNodeProcessDao.getSubprocessesRecursive(cp);
            for (CurrentProcess subProcess : subprocesses) {
                jobs.addAll(jobDao.findByProcess(subProcess));
            }
        }
        List<WfJob> result = Lists.newArrayList();
        for (Job job : jobs) {
            result.add(new WfJob(job));
        }
        return result;
    }

    public List<WfToken> getTokens(User user, Long processId, boolean recursive, boolean toPopulateExecutionErrors)
            throws ProcessDoesNotExistException
    {
        // Search both current and archive even if toPopulateExecutionErrors == true, to check permissions.
        Process process = processDao.getNotNull(processId);
        permissionDao.checkAllowed(user, Permission.LIST, process);

        // Optimization: erroneous processes don't go to archive.
        if (toPopulateExecutionErrors && process.isArchive()) {
            return Collections.emptyList();
        }

        val result = new ArrayList<WfToken>(getTokens(process));
        if (recursive) {
            List<? extends Process> subprocesses = nodeProcessDao.getSubprocessesRecursive(process);
            for (Process subProcess : subprocesses) {
                result.addAll(getTokens(subProcess));
            }
        }
        return result;
    }

    public Long startProcess(User user, String definitionName, Map<String, Object> variables) {
        return startProcessImpl(user, getLatestDefinition(definitionName), variables);
    }

    public Long startProcess(User user, Long processDefinitionVersionId, Map<String, Object> variables) {
        return startProcessImpl(user, getDefinition(processDefinitionVersionId), variables);
    }

    private Long startProcessImpl(User user, ParsedProcessDefinition parsedProcessDefinition, Map<String, Object> variables) {
        if (variables == null) {
            variables = Maps.newHashMap();
        }
        if (SystemProperties.isCheckProcessStartPermissions()) {
            permissionDao.checkAllowed(user, Permission.START, parsedProcessDefinition.getProcessDefinition());
        }
        String transitionName = (String) variables.remove(WfProcess.SELECTED_TRANSITION_KEY);
        val extraVariablesMap = new HashMap<String, Object>();
        extraVariablesMap.put(WfProcess.SELECTED_TRANSITION_KEY, transitionName);
        VariableProvider variableProvider = new MapDelegableVariableProvider(extraVariablesMap, new DefinitionVariableProvider(parsedProcessDefinition));
        validateVariables(user, null, variableProvider, parsedProcessDefinition, parsedProcessDefinition.getStartStateNotNull().getNodeId(), variables);
        // transient variables
        Map<String, Object> transientVariables = (Map<String, Object>) variables.remove(WfProcess.TRANSIENT_VARIABLES);
        CurrentProcess process = processFactory.startProcess(parsedProcessDefinition, variables, user.getActor(), transitionName, transientVariables);
        SwimlaneDefinition startTaskSwimlaneDefinition = parsedProcessDefinition.getStartStateNotNull().getFirstTaskNotNull().getSwimlane();
        Object predefinedProcessStarterObject = variables.get(startTaskSwimlaneDefinition.getName());
        if (predefinedProcessStarterObject != null) {
            Executor predefinedProcessStarter = TypeConversionUtil.convertTo(Executor.class, predefinedProcessStarterObject);
            ExecutionContext executionContext = new ExecutionContext(parsedProcessDefinition, process);
            CurrentSwimlane swimlane = currentSwimlaneDao.findOrCreate(process, startTaskSwimlaneDefinition);
            swimlane.assignExecutor(executionContext, predefinedProcessStarter, true);
        }
        log.info(process + " was successfully started by " + user);
        return process.getId();
    }

    public byte[] getProcessDiagram(User user, Long processId, Long taskId, Long childProcessId, String subprocessId) {
        try {
            Process process = processDao.getNotNull(processId);
            permissionDao.checkAllowed(user, Permission.LIST, process);
            ParsedProcessDefinition parsedProcessDefinition = getDefinition(process);
            Token highlightedToken = null;
            if (taskId != null) {
                Task task = taskDao.get(taskId);
                if (task != null) {
                    log.debug("Task id='" + taskId + "' is null due to completion and graph auto-refresh?");
                    highlightedToken = task.getToken();
                }
            }
            if (childProcessId != null) {
                highlightedToken = nodeProcessDao.findBySubProcessId(childProcessId).getParentToken();
            }
            if (subprocessId != null) {
                parsedProcessDefinition = parsedProcessDefinition.getEmbeddedSubprocessByIdNotNull(subprocessId);
            }
            val processLogs = new ProcessLogs(processId);
            processLogs.addLogs(processLogDao.get(process, parsedProcessDefinition), false);
            GraphImageBuilder builder = new GraphImageBuilder(parsedProcessDefinition);
            builder.setHighlightedToken(highlightedToken);
            return builder.createDiagram(process, processLogs);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public List<NodeGraphElement> getProcessDiagramElements(User user, Long processId, String subprocessId) {
        Process process = processDao.getNotNull(processId);
        ParsedProcessDefinition definition = getDefinition(process.getDefinitionVersion().getId());
        if (subprocessId != null) {
            definition = definition.getEmbeddedSubprocessByIdNotNull(subprocessId);
        }
        List<? extends NodeProcess> nodeProcesses = nodeProcessDao.getNodeProcesses(process, null, null, null);
        ProcessLogs processLogs = null;
        if (DrawProperties.isLogsInGraphEnabled()) {
            processLogs = new ProcessLogs(process.getId());
            ProcessLogFilter filter = new ProcessLogFilter(processId);
            filter.setSeverities(DrawProperties.getLogsInGraphSeverities());
            processLogs.addLogs(processLogDao.getAll(filter), false);
        }
        ProcessGraphInfoVisitor visitor = new ProcessGraphInfoVisitor(user, definition, process, processLogs, nodeProcesses);
        return getDefinitionGraphElements(definition, visitor);
    }

    public NodeGraphElement getProcessDiagramElement(User user, Long processId, String nodeId) {
        Process process = processDao.getNotNull(processId);
        ParsedProcessDefinition definition = getDefinition(process.getDefinitionVersion().getId());
        List<? extends NodeProcess> nodeProcesses = nodeProcessDao.getNodeProcesses(process, null, nodeId, null);
        ProcessLogs processLogs = null;
        if (DrawProperties.isLogsInGraphEnabled()) {
            processLogs = new ProcessLogs(process.getId());
            ProcessLogFilter filter = new ProcessLogFilter(processId);
            filter.setSeverities(DrawProperties.getLogsInGraphSeverities());
            filter.setNodeId(nodeId);
            processLogs.addLogs(processLogDao.getAll(filter), false);
        }
        ProcessGraphInfoVisitor visitor = new ProcessGraphInfoVisitor(user, definition, process, processLogs, nodeProcesses);
        Node node = definition.getNode(nodeId);
        if (node == null) {
            log.warn("No node found by '" + nodeId + "' in " + definition);
            return null;
        }
        NodeGraphElement element = NodeGraphElementBuilder.createElement(node);
        visitor.visit(element);
        return element;
    }

    public byte[] getProcessHistoryDiagram(User user, Long processId, String subprocessId) throws ProcessDoesNotExistException {
        try {
            Process process = processDao.getNotNull(processId);
            permissionDao.checkAllowed(user, Permission.LIST, process);
            ParsedProcessDefinition parsedProcessDefinition = getDefinition(process);
            List<? extends BaseProcessLog> logs = processLogDao.getAll(process);
            List<Executor> executors = executorDao.getAllExecutors(BatchPresentationFactory.EXECUTORS.createNonPaged());
            return new GraphHistoryBuilder(executors, process, parsedProcessDefinition, logs, subprocessId).createDiagram();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public List<NodeGraphElement> getProcessHistoryDiagramElements(User user, Long processId, String subprocessId)
            throws ProcessDoesNotExistException {
        try {
            Process process = processDao.getNotNull(processId);
            permissionDao.checkAllowed(user, Permission.LIST, process);
            ParsedProcessDefinition parsedProcessDefinition = getDefinition(process);
            List<? extends BaseProcessLog> logs = processLogDao.getAll(process);
            List<Executor> executors = executorDao.getAllExecutors(BatchPresentationFactory.EXECUTORS.createNonPaged());
            return new GraphHistoryBuilder(executors, process, parsedProcessDefinition, logs, subprocessId).getElements();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public int upgradeProcessesToDefinitionVersion(User user, Long processDefinitionVersionId, long newVersion) {
        if (!SystemProperties.isUpgradeProcessToDefinitionVersionEnabled()) {
            throw new ConfigurationException(
                    "In order to enable process definition version upgrade set property 'upgrade.process.to.definition.version.enabled' " +
                    "to 'true' in system.properties or wfe.custom.system.properties"
            );
        }
        ProcessDefinitionWithVersion dwv = processDefinitionDao.findDefinition(processDefinitionVersionId);
        ProcessDefinitionWithVersion nextDWV = processDefinitionDao.getByNameAndVersion(dwv.processDefinition.getName(), newVersion);
        ProcessFilter filter = new ProcessFilter();
        filter.setDefinitionName(dwv.processDefinition.getName());
        filter.setDefinitionVersion(dwv.processDefinitionVersion.getVersion());
        filter.setFinished(false);
        List<CurrentProcess> processes = currentProcessDao.getProcesses(filter);
        for (CurrentProcess process : processes) {
            process.setDefinitionVersion(nextDWV.processDefinitionVersion);
            currentProcessDao.update(process);
            processLogDao.addLog(new CurrentAdminActionLog(user.getActor(), CurrentAdminActionLog.ACTION_UPGRADE_PROCESS_TO_VERSION,
                    dwv.processDefinitionVersion.getVersion(), newVersion), process, null);
        }
        return processes.size();
    }

    public boolean upgradeProcessToDefinitionVersion(User user, long processId, Long version) {
        if (!SystemProperties.isUpgradeProcessToDefinitionVersionEnabled()) {
            throw new ConfigurationException(
                    "In order to enable process definition version upgrade set property 'upgrade.process.to.definition.version.enabled' " +
                    "to 'true' in system.properties or wfe.custom.system.properties"
            );
        }
        CurrentProcess process = currentProcessDao.getNotNull(processId);
        // TODO checkPermissionAllowed(user, process, ProcessPermission.UPDATE);
        ProcessDefinitionVersion dv = process.getDefinitionVersion();
        long newVersion = version != null ? version : dv.getVersion() + 1;
        if (newVersion == dv.getVersion()) {
            return false;
        }
        ProcessDefinitionWithVersion nextDWV = processDefinitionDao.getByNameAndVersion(dv.getDefinition().getName(), newVersion);
        process.setDefinitionVersion(nextDWV.processDefinitionVersion);
        currentProcessDao.update(process);
        processLogDao.addLog(new CurrentAdminActionLog(user.getActor(), CurrentAdminActionLog.ACTION_UPGRADE_PROCESS_TO_VERSION, dv.getVersion(),
                newVersion), process, null);
        return true;
    }

    public List<WfSwimlane> getProcessSwimlanes(User user, Long processId) throws ProcessDoesNotExistException {
        Process process = processDao.getNotNull(processId);
        ParsedProcessDefinition parsedProcessDefinition = getDefinition(process);
        permissionDao.checkAllowed(user, Permission.LIST, process);
        List<SwimlaneDefinition> swimlanes = parsedProcessDefinition.getSwimlanes();
        List<WfSwimlane> result = Lists.newArrayListWithExpectedSize(swimlanes.size());
        for (SwimlaneDefinition swimlaneDefinition : swimlanes) {
            Swimlane swimlane = swimlaneDao.findByProcessAndName(process, swimlaneDefinition.getName());
            Executor assignedExecutor = null;
            if (swimlane != null && swimlane.getExecutor() != null) {
                if (permissionDao.isAllowed(user, Permission.LIST, swimlane.getExecutor())) {
                    assignedExecutor = swimlane.getExecutor();
                } else {
                    assignedExecutor = Actor.UNAUTHORIZED_ACTOR;
                }
            }
            result.add(new WfSwimlane(swimlaneDefinition, swimlane, assignedExecutor));
        }
        return result;
    }
    
    public List<WfSwimlane> getActiveProcessesSwimlanes(User user, String namePattern) {
        List<CurrentSwimlane> list = currentSwimlaneDao.findByNamePatternInActiveProcesses(namePattern);
        List<WfSwimlane> listSwimlanes = Lists.newArrayList();
        for (Swimlane swimlane : list) {
            ParsedProcessDefinition processDefinition = getDefinition(swimlane.getProcess());
            SwimlaneDefinition swimlaneDefinition = processDefinition.getSwimlaneNotNull(swimlane.getName());
            Executor assignedExecutor = swimlane.getExecutor();
            if (assignedExecutor == null || !permissionDao.isAllowed(user, Permission.LIST, assignedExecutor)) {
                assignedExecutor = Actor.UNAUTHORIZED_ACTOR;
            }
            listSwimlanes.add(new WfSwimlane(swimlaneDefinition, swimlane, assignedExecutor));
        }
        return listSwimlanes;
    }
    
    public boolean reassignSwimlane(User user, Long id) {
        CurrentSwimlane swimlane = currentSwimlaneDao.get(id);
        Process process = swimlane.getProcess();
        ParsedProcessDefinition processDefinition = getDefinition(process);
        Delegation delegation = processDefinition.getSwimlaneNotNull(swimlane.getName()).getDelegation();
        Executor oldExecutor = swimlane.getExecutor();
        try {
            AssignmentHandler handler = delegation.getInstance();
            handler.assign(new ExecutionContext(processDefinition, process), swimlane);
        } catch (Exception e) {
            log.error("Unable to reassign swimlane " + id, e);
        }
        return !Objects.equal(oldExecutor, swimlane.getExecutor());
    }

    public void assignSwimlane(User user, Long processId, String swimlaneName, Executor executor) {
        CurrentProcess process = currentProcessDao.getNotNull(processId);
        ParsedProcessDefinition parsedProcessDefinition = getDefinition(process);
        SwimlaneDefinition swimlaneDefinition = parsedProcessDefinition.getSwimlaneNotNull(swimlaneName);
        CurrentSwimlane swimlane = currentSwimlaneDao.findOrCreate(process, swimlaneDefinition);
        List<Executor> executors = executor != null ? Lists.newArrayList(executor) : null;
        AssignmentHelper.assign(new ExecutionContext(parsedProcessDefinition, process), swimlane, executors);
    }

    public void activateProcess(User user, Long processId) {
        if (!executorLogic.isAdministrator(user)) {
            throw new InternalApplicationException("Only administrator can activate process");
        }
        CurrentProcess process = currentProcessDao.getNotNull(processId);
        boolean resetCaches = process.getExecutionStatus() == ExecutionStatus.SUSPENDED;
        activateProcessWithSubprocesses(user, process);
        if (resetCaches) {
            TransactionListeners.addListener(new CacheResetTransactionListener(), true);
        }
        log.info("Process " + processId + " activated");
    }

    public void suspendProcess(User user, Long processId) {
        if (!SystemProperties.isProcessSuspensionEnabled()) {
            throw new InternalApplicationException("process suspension disabled in settings");
        }
        if (!executorLogic.isAdministrator(user)) {
            throw new InternalApplicationException("Only administrator can suspend process");
        }
        suspendProcessWithSubprocesses(user, currentProcessDao.getNotNull(processId));
        TransactionListeners.addListener(new CacheResetTransactionListener(), true);
        log.info("Process " + processId + " suspended");
    }

    public List<WfProcess> getFailedProcesses(User user) {
        BatchPresentation batchPresentation = BatchPresentationFactory.CURRENT_PROCESSES.createNonPaged();
        int index = batchPresentation.getType().getFieldIndex(CurrentProcessClassPresentation.PROCESS_EXECUTION_STATUS);
        batchPresentation.getFilteredFields().put(index, new StringFilterCriteria(ExecutionStatus.FAILED.name()));
        List<CurrentProcess> processes = getPersistentObjects(user, batchPresentation, Permission.LIST, PROCESS_EXECUTION_CLASSES, false);
        return toWfProcesses(processes, null);
    }

    public List<CurrentToken> findTokensForMessageSelector(Map<String, String> routingData) {
        if (SystemProperties.isProcessExecutionMessagePredefinedSelectorEnabled()) {
            if (SystemProperties.isProcessExecutionMessagePredefinedSelectorOnlyStrictComplianceHandling()) {
                String messageSelector = Utils.getObjectMessageStrictSelector(routingData);
                return currentTokenDao.findByMessageSelectorAndExecutionStatusIsActive(messageSelector);
            } else {
                Set<String> messageSelectors = Utils.getObjectMessageCombinationSelectors(routingData);
                return currentTokenDao.findByMessageSelectorInAndExecutionStatusIsActive(messageSelectors);
            }
        } else {
            throw new InternalApplicationException("Method not implemented for process.execution.message.predefined.selector.enabled = false");
        }
    }

    private List<WfToken> getTokens(Process process) throws ProcessDoesNotExistException {
        List<WfToken> result = Lists.newArrayList();
        List<? extends Token> tokens = tokenDao.findByProcessAndExecutionStatusIsNotEnded(process);
        ParsedProcessDefinition parsedProcessDefinition = processDefinitionLoader.getDefinition(process);
        for (Token token : tokens) {
            result.add(new WfToken(token, parsedProcessDefinition));
        }
        return result;
    }

    private List<CurrentProcess> getCurrentProcessesInternal(User user, ProcessFilter filter) {
        List<CurrentProcess> processes = currentProcessDao.getProcesses(filter);
        processes = filterSecuredObject(user, processes, Permission.LIST);
        return processes;
    }

    private List<WfProcess> toWfProcesses(List<? extends Process> processes, List<String> variableNamesToInclude) {
        List<WfProcess> result = Lists.newArrayListWithExpectedSize(processes.size());
        for (Process process : processes) {
            WfProcess wfProcess = new WfProcess(process);
            if (!Utils.isNullOrEmpty(variableNamesToInclude)) {
                try {
                    ParsedProcessDefinition parsedProcessDefinition = getDefinition(process);
                    Map<Process, Map<String, Variable>> variables = variableDao.getVariables(processes, variableNamesToInclude);
                    ExecutionContext executionContext = new ExecutionContext(parsedProcessDefinition, process, variables, false);
                    for (String variableName : variableNamesToInclude) {
                        try {
                            wfProcess.addVariable(executionContext.getVariableProvider().getVariable(variableName));
                        } catch (Exception e) {
                            log.error("Unable to get '" + variableName + "' in " + process, e);
                        }
                    }
                } catch (Exception e) {
                    log.error("Unable to get variables in " + process, e);
                }
            }
            result.add(wfProcess);
        }
        return result;
    }

    private void activateProcessWithSubprocesses(User user, CurrentProcess process) {
        if (process.getExecutionStatus() == ExecutionStatus.ENDED) {
            return;
        }
        if (process.getExecutionStatus() == ExecutionStatus.ACTIVE) {
            throw new InternalApplicationException(process + " already activated");
        }
        for (CurrentToken token : currentTokenDao.findByProcessAndExecutionStatus(process, ExecutionStatus.FAILED)) {
            nodeAsyncExecutor.execute(token, false);
        }
        for (CurrentToken token : currentTokenDao.findByProcessAndExecutionStatus(process, ExecutionStatus.SUSPENDED)) {
            token.setExecutionStatus(ExecutionStatus.ACTIVE);
        }
        if (process.getExecutionStatus() == ExecutionStatus.SUSPENDED) {
            process.setExecutionStatus(ExecutionStatus.ACTIVE);
        }
        processLogDao.addLog(new CurrentProcessActivateLog(user.getActor()), process, null);
        List<CurrentProcess> subprocesses = currentNodeProcessDao.getSubprocessesRecursive(process);
        for (CurrentProcess subprocess : subprocesses) {
            if (subprocess.getExecutionStatus() != ExecutionStatus.ACTIVE) {
                activateProcessWithSubprocesses(user, subprocess);
            }
        }
    }

    private void suspendProcessWithSubprocesses(User user, CurrentProcess process) {
        if (process.getExecutionStatus() == ExecutionStatus.SUSPENDED) {
            throw new InternalApplicationException(process + " already suspended");
        }
        if (process.getExecutionStatus() == ExecutionStatus.ENDED) {
            return;
        }
        process.setExecutionStatus(ExecutionStatus.SUSPENDED);
        for (CurrentToken token : currentTokenDao.findByProcessAndExecutionStatus(process, ExecutionStatus.ACTIVE)) {
            token.setExecutionStatus(ExecutionStatus.SUSPENDED);
        }
        processLogDao.addLog(new CurrentProcessSuspendLog(user.getActor()), process, null);
        List<CurrentProcess> subprocesses = currentNodeProcessDao.getSubprocessesRecursive(process);
        for (CurrentProcess subprocess : subprocesses) {
            if (subprocess.getExecutionStatus() != ExecutionStatus.SUSPENDED) {
                suspendProcessWithSubprocesses(user, subprocess);
            }
        }
    }
}
