package ru.runa.wfe.user;

import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.DefaultDbSource;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.security.Permission;

/**
 * Created on 22.10.2005
 */
public class ExecutorClassPresentation extends ClassPresentation {
    public static final String NAME = "batch_presentation.executor.name";
    public static final String FULL_NAME = "batch_presentation.executor.full_name";
    public static final String DESCRIPTION = "batch_presentation.executor.description";
    public static final String TYPE = "batch_presentation.executor.type";

    public static final ClassPresentation INSTANCE = new ExecutorClassPresentation();

    private ExecutorClassPresentation() {
        super(Executor.class, "", true, new FieldDescriptor[] {
                // display name field type DB source isSort filter mode get
                // value/show in web getter param
                new FieldDescriptor(NAME, String.class.getName(), new DefaultDbSource(Executor.class, "name"), true, 1, BatchPresentationConsts.ASC,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.NONE, "name" }),
                new FieldDescriptor(FULL_NAME, String.class.getName(), new DefaultDbSource(Executor.class, "fullName"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.NONE, "fullName" }),
                new FieldDescriptor(DESCRIPTION, String.class.getName(), new DefaultDbSource(Executor.class, "description"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.NONE, "description" }),
                new FieldDescriptor(TYPE, String.class.getName(), new DefaultDbSource(Executor.class, "class"), false, FieldFilterMode.DATABASE,
                        "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.NONE, "class" }).setShowable(false) });
    }
}
