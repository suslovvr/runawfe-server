package ru.runa.wfe.lang.bpmn2;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.audit.CurrentActionLog;
import ru.runa.wfe.audit.CurrentCreateTimerLog;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.Errors;
import ru.runa.wfe.commons.bc.BusinessCalendar;
import ru.runa.wfe.commons.bc.BusinessDuration;
import ru.runa.wfe.commons.bc.BusinessDurationParser;
import ru.runa.wfe.commons.error.ProcessError;
import ru.runa.wfe.commons.error.ProcessErrorType;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandler;
import ru.runa.wfe.job.TimerJob;
import ru.runa.wfe.job.dao.JobDao;
import ru.runa.wfe.lang.BoundaryEvent;
import ru.runa.wfe.lang.BoundaryEventContainer;
import ru.runa.wfe.lang.Delegation;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.task.TaskCompletionInfo;

public class TimerNode extends Node implements BoundaryEventContainer, BoundaryEvent {
    private static final long serialVersionUID = 1L;

    private Boolean boundaryEventInterrupting;
    private String dueDateExpression;
    private String repeatDurationString;
    private Delegation actionDelegation;
    private final List<BoundaryEvent> boundaryEvents = Lists.newArrayList();

    @Autowired
    private transient JobDao jobDao;

    @Override
    public List<BoundaryEvent> getBoundaryEvents() {
        return boundaryEvents;
    }

    @Override
    public Boolean getBoundaryEventInterrupting() {
        return boundaryEventInterrupting;
    }

    @Override
    public void setBoundaryEventInterrupting(Boolean boundaryEventInterrupting) {
        this.boundaryEventInterrupting = boundaryEventInterrupting;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.TIMER;
    }

    public String getDueDateExpression() {
        return dueDateExpression;
    }

    public void setDueDateExpression(String dueDateExpression) {
        this.dueDateExpression = dueDateExpression;
    }

    public void setRepeatDurationString(String repeatDurationString) {
        this.repeatDurationString = repeatDurationString;
    }

    public void setActionDelegation(Delegation actionDelegation) {
        this.actionDelegation = actionDelegation;
    }

    @Override
    protected void execute(ExecutionContext executionContext) throws Exception {
        TimerJob timerJob = new TimerJob(executionContext.getCurrentToken());
        timerJob.setName(getName());
        timerJob.setDueDateExpression(dueDateExpression);
        timerJob.setDueDate(ExpressionEvaluator.evaluateDueDate(executionContext.getVariableProvider(), dueDateExpression));
        timerJob.setRepeatDurationString(repeatDurationString);
        jobDao.create(timerJob);
        log.debug("Created " + timerJob);
        executionContext.addLog(new CurrentCreateTimerLog(timerJob.getDueDate()));
    }

    @Override
    public void cancelBoundaryEvent(CurrentToken token) {
        jobDao.deleteByToken(token);
        // TODO 212
        // executionContext.addLog(new CurrentActionLog(this));
    }

    @Override
    public TaskCompletionInfo getTaskCompletionInfoIfInterrupting() {
        return TaskCompletionInfo.createForTimer();
    }

    public void onTimerJob(ExecutionContext executionContext, TimerJob timerJob) {
        ProcessError processError = new ProcessError(ProcessErrorType.system, timerJob.getProcess().getId(), getNodeId());
        try {
            if (actionDelegation != null) {
                try {
                    ActionHandler actionHandler = actionDelegation.getInstance();
                    log.debug("Executing delegation in " + this);
                    actionHandler.execute(executionContext);
                    executionContext.addLog(new CurrentActionLog(this));
                } catch (Exception e) {
                    log.error("Failed " + this);
                    throw Throwables.propagate(e);
                }
            }
            if (!getLeavingTransitions().isEmpty()) {
                cancelBoundaryEvent(executionContext.getCurrentToken());
                leave(executionContext);
            } else if (Boolean.TRUE.equals(executionContext.getTransientVariable(TimerJob.STOP_RE_EXECUTION))) {
                log.debug("Deleting " + timerJob + " due to STOP_RE_EXECUTION");
                cancelBoundaryEvent(executionContext.getCurrentToken());
            } else if (repeatDurationString != null) {
                // restart timer
                BusinessDuration repeatDuration = BusinessDurationParser.parse(repeatDurationString);
                if (repeatDuration.getAmount() > 0) {
                    BusinessCalendar businessCalendar = ApplicationContextFactory.getBusinessCalendar();
                    // clear expression for ignorance from
                    // ExecutionContext.updateRelatedObjectsDueToDateVariableChange
                    timerJob.setDueDateExpression(null);
                    timerJob.setDueDate(businessCalendar.apply(timerJob.getDueDate(), repeatDurationString));
                    log.debug("Restarting " + timerJob + " for repeat execution at " + CalendarUtil.formatDateTime(timerJob.getDueDate()));
                }
            } else {
                log.debug("Deleting " + timerJob + " after execution");
                cancelBoundaryEvent(executionContext.getCurrentToken());
            }
            Errors.removeProcessError(processError);
        } catch (Throwable th) {
            String nodeName;
            try {
                nodeName = executionContext.getParsedProcessDefinition().getNodeNotNull(getNodeId()).getName();
            } catch (Exception e) {
                nodeName = "Unknown due to " + e;
            }
            Errors.addProcessError(processError, nodeName, th);
            throw Throwables.propagate(th);
        }
    }
}
