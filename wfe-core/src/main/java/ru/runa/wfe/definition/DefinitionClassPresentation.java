package ru.runa.wfe.definition;

import java.util.Date;
import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.DefaultDbSource;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.presentation.filter.AnywhereStringFilterCriteria;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.user.Actor;

/**
 * Class presentation for process definition. Shows only last deployed process definition.
 *
 * @author Dofs
 * @since 4.0
 */
public class DefinitionClassPresentation extends ClassPresentation {
    public static final String NAME = "batch_presentation.process_definition.name";
    public static final String DESCRIPTION = "batch_presentation.process_definition.description";
    public static final String VERSION = "batch_presentation.process_definition.version";
    public static final String TYPE = "batch_presentation.process_definition.process_type";
    public static final String CREATE_DATE = "batch_presentation.process_definition.create_date";
    public static final String CREATE_ACTOR = "batch_presentation.process_definition.create_actor";
    public static final String UPDATE_DATE = "batch_presentation.process_definition.update_date";
    public static final String UPDATE_ACTOR = "batch_presentation.process_definition.update_actor";
    public static final String SUBPROCESS_BINDING_DATE = "batch_presentation.process_definition.subprocess_binding_date";

    public static final ClassPresentation INSTANCE = new DefinitionClassPresentation();

    private DefinitionClassPresentation() {
        super(ProcessDefinition.class, null, false, new FieldDescriptor[] {
                // display name field type DB source isSort filter mode
                // get value/show in web getter parameters
                new FieldDescriptor(NAME, String.class.getName(), new DefaultDbSource(ProcessDefinition.class, "name"), true, 1,
                        BatchPresentationConsts.ASC, FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[] {
                        Permission.START, "name" }),
                new FieldDescriptor(DESCRIPTION, String.class.getName(), new DefaultDbSource(ProcessDefinition.class, "description"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.DescriptionProcessTdBuilder", new Object[] {}),
                new FieldDescriptor(TYPE, AnywhereStringFilterCriteria.class.getName(), new DefaultDbSource(ProcessDefinition.class, "category"),
                        true, FieldFilterMode.DATABASE, "ru.runa.wf.web.html.CategoryTdBuilder", new Object[] {}),
                new FieldDescriptor(CREATE_DATE, Date.class.getName(), new DefaultDbSource(ProcessDefinition.class, "latestVersion.createDate"),
                        true, FieldFilterMode.DATABASE, "ru.runa.wf.web.html.DefinitionCreateDateTdBuilder", new Object[] {}),
                new FieldDescriptor(CREATE_ACTOR, Actor.class.getName(), new DefaultDbSource(ProcessDefinition.class, "latestVersion.createActor"),
                        false, FieldFilterMode.NONE, "ru.runa.wf.web.html.DefinitionCreateActorTdBuilder", new Object[] {}),
                new FieldDescriptor(UPDATE_DATE, Date.class.getName(), new DefaultDbSource(ProcessDefinition.class, "latestVersion.updateDate"),
                        true, FieldFilterMode.DATABASE, "ru.runa.wf.web.html.DefinitionUpdateDateTdBuilder", new Object[] {}),
                new FieldDescriptor(UPDATE_ACTOR, Actor.class.getName(), new DefaultDbSource(ProcessDefinition.class, "latestVersion.updateActor"),
                        false, FieldFilterMode.NONE, "ru.runa.wf.web.html.DefinitionUpdateActorTdBuilder", new Object[] {}),
                new FieldDescriptor(SUBPROCESS_BINDING_DATE, Date.class.getName(), new DefaultDbSource(ProcessDefinition.class,
                        "latestVersion.subprocessBindingDate"), true, FieldFilterMode.DATABASE,
                        "ru.runa.wf.web.html.DefinitionSubprocessBindingDateTdBuilder", new Object[] {}).setVisible(false)
        });
    }
}
