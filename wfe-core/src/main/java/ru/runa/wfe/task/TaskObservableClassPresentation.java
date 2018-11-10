package ru.runa.wfe.task;

import java.util.Date;
import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.DefaultDbSource;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.presentation.VariableDbSources;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.var.CurrentVariable;

/**
 * Created on 2017-04-05
 */
public class TaskObservableClassPresentation extends ClassPresentation {

    public static final String NAME = "batch_presentation.task.name";
    public static final String DESCRIPTION = "batch_presentation.task.description";
    public static final String DEFINITION_NAME = "batch_presentation.task.definition_name";
    public static final String PROCESS_ID = "batch_presentation.task.process_id";
    public static final String OWNER = "batch_presentation.task.owner";
    public static final String TASK_SWIMLINE = "batch_presentation.task.swimlane";
    public static final String TASK_VARIABLE = editable_prefix + "name:batch_presentation.task.variable";
    public static final String TASK_DEADLINE = "batch_presentation.task.deadline";
    public static final String TASK_CREATE_DATE = "batch_presentation.task.create_date";
    public static final String TASK_ASSIGN_DATE = "batch_presentation.task.assign_date";
    public static final String TASK_DURATION = "batch_presentation.task.duration";
    public static final String TASK_OBSERVABLE_EXECUTOR = "batch_presentation.task.observable_executor";

    public static final ClassPresentation INSTANCE = new TaskObservableClassPresentation();

    private TaskObservableClassPresentation() {
        super(Task.class, "", false, new FieldDescriptor[] {
                new FieldDescriptor(NAME, String.class.getName(), new DefaultDbSource(Task.class, "name"), true, 3, BatchPresentationConsts.ASC,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.NONE, "name" }),
                new FieldDescriptor(DESCRIPTION, String.class.getName(), new DefaultDbSource(Task.class, "description"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.TaskDescriptionTdBuilder", new Object[] {}),
                new FieldDescriptor(DEFINITION_NAME, String.class.getName(), new DefaultDbSource(Task.class, "process.definitionVersion.definition.name"),
                        true, FieldFilterMode.DATABASE, "ru.runa.wf.web.html.TaskProcessDefinitionTdBuilder", new Object[] {}),
                new FieldDescriptor(PROCESS_ID, Integer.class.getName(), new DefaultDbSource(Task.class, "process.id"), true, 2,
                        BatchPresentationConsts.ASC, FieldFilterMode.DATABASE, "ru.runa.wf.web.html.TaskProcessIdTdBuilder", new Object[] {}),
                new FieldDescriptor(OWNER, String.class.getName(), new DefaultDbSource(Task.class, "executor.name"), true, FieldFilterMode.DATABASE,
                        "ru.runa.wf.web.html.TaskOwnerTdBuilder", new Object[] {}),
                new FieldDescriptor(TASK_SWIMLINE, String.class.getName(), new DefaultDbSource(Task.class, "swimlane.name"), false,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.TaskRoleTdBuilder", new Object[] {}),
                new FieldDescriptor(TASK_VARIABLE, CurrentVariable.class.getName(), VariableDbSources.get("process"), true, FieldFilterMode.DATABASE,
                        "ru.runa.wf.web.html.TaskVariableTdBuilder", new Object[] {}),
                new FieldDescriptor(TASK_DEADLINE, Date.class.getName(), new DefaultDbSource(Task.class, "deadlineDate"), true, 1,
                        BatchPresentationConsts.DESC, FieldFilterMode.DATABASE, "ru.runa.wf.web.html.TaskDeadlineTdBuilder", new Object[] {}),
                new FieldDescriptor(TASK_CREATE_DATE, Date.class.getName(), new DefaultDbSource(Task.class, "createDate"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.TaskCreationDateTdBuilder", new Object[] {}),
                new FieldDescriptor(TASK_ASSIGN_DATE, Date.class.getName(), new DefaultDbSource(Task.class, null), false, FieldFilterMode.NONE,
                        "ru.runa.wf.web.html.TaskAssignmentDateTdBuilder", new Object[] {}).setVisible(false),
                new FieldDescriptor(TASK_DURATION, String.class.getName(), new DefaultDbSource(Task.class, null), false, FieldFilterMode.NONE,
                        "ru.runa.wf.web.html.TaskDurationTdBuilder", new Object[] {}).setVisible(false),
                new FieldDescriptor(TASK_OBSERVABLE_EXECUTOR, String.class.getName(), new DefaultDbSource(Task.class, null), false,
                        FieldFilterMode.APPLICATION, "ru.runa.common.web.html.PropertyTdBuilder", new Object[] {}).setShowable(false) });
    }
}
