package ru.runa.wfe.task;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import java.util.Date;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import lombok.extern.apachecommons.CommonsLog;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CollectionOfElements;
import ru.runa.wfe.audit.CurrentTaskAssignLog;
import ru.runa.wfe.audit.CurrentTaskCancelledLog;
import ru.runa.wfe.audit.CurrentTaskEndByAdminLog;
import ru.runa.wfe.audit.CurrentTaskEndBySubstitutorLog;
import ru.runa.wfe.audit.CurrentTaskEndLog;
import ru.runa.wfe.audit.CurrentTaskExpiredLog;
import ru.runa.wfe.audit.CurrentTaskRemovedOnProcessEndLog;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.CurrentSwimlane;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.Assignable;
import ru.runa.wfe.extension.assign.AssignmentHelper;
import ru.runa.wfe.lang.ActionEvent;
import ru.runa.wfe.lang.BaseTaskNode;
import ru.runa.wfe.lang.InteractionNode;
import ru.runa.wfe.lang.TaskDefinition;
import ru.runa.wfe.task.logic.TaskNotifier;
import ru.runa.wfe.user.Executor;

/**
 * is one task that can be assigned to an actor (read: put in someone's task list) and that can trigger the continuation of execution of the token
 * upon completion.
 */
@Entity
@Table(name = "BPM_TASK")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@CommonsLog
public class Task implements Assignable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long version;
    private String nodeId;
    private String name;
    private String description;
    private Executor executor;
    private Date createDate;
    private Date deadlineDate;
    private Date assignDate;
    private String deadlineDateExpression;
    private CurrentToken token;
    private CurrentSwimlane swimlane;
    private CurrentProcess process;
    private Set<Long> openedByExecutorIds;
    private Integer index;

    public Task() {
    }

    public Task(CurrentToken token, TaskDefinition taskDefinition) {
        setToken(token);
        setProcess(token.getProcess());
        setNodeId(taskDefinition.getNodeId());
        setCreateDate(new Date());
        openedByExecutorIds = Sets.newHashSet();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_TASK", allocationSize = 1)
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = id;
    }

    @Version
    @Column(name = "VERSION")
    protected Long getVersion() {
        return version;
    }

    protected void setVersion(Long version) {
        this.version = version;
    }

    @Column(name = "NODE_ID", length = 1024)
    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    @Column(name = "NAME", length = 1024)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "DESCRIPTION", length = 1024)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "CREATE_DATE", nullable = false)
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Column(name = "DEADLINE_DATE")
    public Date getDeadlineDate() {
        return deadlineDate;
    }

    public void setDeadlineDate(Date deadlineDate) {
        this.deadlineDate = deadlineDate;
    }

    @Column(name = "ASSIGN_DATE")
    public Date getAssignDate() {
        return assignDate;
    }

    public void setAssignDate(Date assignDate) {
        this.assignDate = assignDate;
    }

    @Column(name = "DEADLINE_DATE_EXPRESSION")
    public String getDeadlineDateExpression() {
        return deadlineDateExpression;
    }

    public void setDeadlineDateExpression(String deadlineDateExpression) {
        this.deadlineDateExpression = deadlineDateExpression;
    }

    @CollectionOfElements
    @JoinTable(name = "BPM_TASK_OPENED", joinColumns = { @JoinColumn(name = "TASK_ID", nullable = false, updatable = false) })
    @Column(name = "EXECUTOR_ID", updatable = false)
    public Set<Long> getOpenedByExecutorIds() {
        return openedByExecutorIds;
    }

    public void setOpenedByExecutorIds(Set<Long> openedByExecutorIds) {
        this.openedByExecutorIds = openedByExecutorIds;
    }

    @ManyToOne(targetEntity = CurrentToken.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "TOKEN_ID")
    public CurrentToken getToken() {
        return token;
    }

    public void setToken(CurrentToken token) {
        this.token = token;
    }

    @ManyToOne(targetEntity = CurrentSwimlane.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "SWIMLANE_ID")
    public CurrentSwimlane getSwimlane() {
        return swimlane;
    }

    public void setSwimlane(CurrentSwimlane swimlane) {
        this.swimlane = swimlane;
    }

    @ManyToOne(targetEntity = CurrentProcess.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "PROCESS_ID")
    public CurrentProcess getProcess() {
        return process;
    }

    public void setProcess(CurrentProcess process) {
        this.process = process;
    }

    @Override
    @ManyToOne(targetEntity = Executor.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "EXECUTOR_ID")
    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    @Column(name = "TASK_INDEX")
    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    @Transient
    @Override
    public String getSwimlaneName() {
        return swimlane.getName();
    }

    @Override
    public void assignExecutor(ExecutionContext executionContext, Executor executor, boolean cascadeUpdate) {
        if (!Objects.equal(getExecutor(), executor)) {
            log.debug("assigning " + this + " to " + executor);
            Executor previousExecutor = getExecutor();
            // log this assignment
            executionContext.addLog(new CurrentTaskAssignLog(this, executor));
            // do the actual assignment
            setExecutor(executor);
            setAssignDate(new Date());
            InteractionNode node = (InteractionNode) executionContext.getParsedProcessDefinition().getNodeNotNull(nodeId);
            ExecutionContext taskExecutionContext = new ExecutionContext(executionContext.getParsedProcessDefinition(), this);
            node.getFirstTaskNotNull().fireEvent(taskExecutionContext, ActionEvent.TASK_ASSIGN);
            for (TaskNotifier notifier : ApplicationContextFactory.getTaskNotifiers()) {
                notifier.onTaskAssigned(executionContext.getParsedProcessDefinition(), executionContext.getVariableProvider(), this, previousExecutor);
            }
        }
        if (cascadeUpdate && swimlane != null) {
            swimlane.assignExecutor(executionContext, executor, false);
        }
    }

    /**
     * marks this task as done and specifies a transition leaving the task-node for the case that the completion of this tasks triggers a signal on
     * the token. If this task leads to a signal on the token, the given transition name will be used in the signal. If this task completion does not
     * trigger execution to move on, the transition is ignored.
     */
    public void end(ExecutionContext executionContext, BaseTaskNode taskNode, TaskCompletionInfo completionInfo) {
        log.debug("Ending " + this + " with " + completionInfo);
        switch (completionInfo.getCompletionBy()) {
        case TIMER:
            executionContext.addLog(new CurrentTaskExpiredLog(this, completionInfo));
            break;
        case ASSIGNED_EXECUTOR:
            executionContext.addLog(new CurrentTaskEndLog(this, completionInfo));
            break;
        case SUBSTITUTOR:
            executionContext.addLog(new CurrentTaskEndBySubstitutorLog(this, completionInfo));
            break;
        case ADMIN:
            executionContext.addLog(new CurrentTaskEndByAdminLog(this, completionInfo));
            break;
        case HANDLER:
            executionContext.addLog(new CurrentTaskCancelledLog(this, completionInfo));
            break;
        case PROCESS_END:
            executionContext.addLog(new CurrentTaskRemovedOnProcessEndLog(this, completionInfo));
            break;
        default:
            throw new IllegalArgumentException("Unimplemented for " + completionInfo.getCompletionBy());
        }
        if (completionInfo.getCompletionBy() != TaskCompletionBy.PROCESS_END) {
            ExecutionContext taskExecutionContext = new ExecutionContext(executionContext.getParsedProcessDefinition(), this);
            taskNode.getFirstTaskNotNull().fireEvent(taskExecutionContext, ActionEvent.TASK_END);
        }
        delete();
    }

    public void delete() {
        ApplicationContextFactory.getTaskDao().delete(this);
        AssignmentHelper.removeTemporaryGroupOnTaskEnd(getExecutor());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", id).add("name", name).add("assignedTo", executor).toString();
    }
}
