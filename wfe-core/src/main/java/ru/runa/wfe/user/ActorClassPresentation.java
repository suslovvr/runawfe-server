package ru.runa.wfe.user;

import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.DefaultDbSource;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.security.Permission;

/**
 * Class presentation for actors.
 * 
 * @author dofs
 * @since 4.0
 */
public class ActorClassPresentation extends ClassPresentation {
    public static final String NAME = "batch_presentation.actor.name";
    public static final String FULL_NAME = "batch_presentation.actor.full_name";
    public static final String DESCRIPTION = "batch_presentation.actor.description";

    public static final ClassPresentation INSTANCE = new ActorClassPresentation();

    private ActorClassPresentation() {
        super(Actor.class, "", true, new FieldDescriptor[] {
                new FieldDescriptor(NAME, String.class.getName(), new DefaultDbSource(Actor.class, "name"), true,1, BatchPresentationConsts.ASC,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.NONE, "name" }),
                new FieldDescriptor(FULL_NAME, String.class.getName(), new DefaultDbSource(Actor.class, "fullName"), true, FieldFilterMode.DATABASE,
                        "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.NONE, "fullName" }),
                new FieldDescriptor(DESCRIPTION, String.class.getName(), new DefaultDbSource(Actor.class, "description"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.NONE, "description" }) });
    }
}
