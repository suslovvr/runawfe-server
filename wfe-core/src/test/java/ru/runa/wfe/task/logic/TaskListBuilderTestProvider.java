package ru.runa.wfe.task.logic;

import java.util.Set;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.cache.TaskCacheCtrl;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.EscalationGroup;
import ru.runa.wfe.user.Executor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TaskListBuilderTestProvider extends TaskListBuilderImpl implements ITaskListBuilderTestProvider {

    private static TaskCacheCtrl taskCache = mock(TaskCacheCtrl.class);

    static {
        when(taskCache.getTasks(any(Long.class), any(BatchPresentation.class))).thenReturn(null);
    }

    public TaskListBuilderTestProvider() {
        super(taskCache);
    }

    @Override
    public TaskInListState getAcceptableTask(Task task, Actor actor, BatchPresentation batchPresentation, Set<Executor> executorsToGetTasksByMembership) {
        return super.getAcceptableTask(task, actor, batchPresentation, executorsToGetTasksByMembership);
    }

    @Override
    public boolean isTaskAcceptableBySubstitutionRules(ExecutionContext executionContext, Task task, Actor assignedActor, Actor substitutorActor) {
        return super.isTaskAcceptableBySubstitutionRules(executionContext, task, assignedActor, substitutorActor);
    }

    @Override
    public int checkSubstitutionRules(SubstitutionCriteria criteria, Set<Long> ids, ExecutionContext executionContext, Task task,
            Actor assignedActor, Actor substitutorActor) {
        return super.checkSubstitutionRules(criteria, ids, executionContext, task, assignedActor, substitutorActor);
    }

    @Override
    public boolean isActorInInactiveEscalationGroup(Actor actor, EscalationGroup group) {
        return super.isActorInInactiveEscalationGroup(actor, group);
    }

}
