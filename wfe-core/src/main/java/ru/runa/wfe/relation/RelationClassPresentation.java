package ru.runa.wfe.relation;

import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.DefaultDbSource;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.security.Permission;

public class RelationClassPresentation extends ClassPresentation {
    public static final String NAME = "batch_presentation.relation.name";
    public static final String DESCRIPTION = "batch_presentation.relation.description";

    public static final ClassPresentation INSTANCE = new RelationClassPresentation();

    private RelationClassPresentation() {
        super(Relation.class, "", false, new FieldDescriptor[] {
                // display name field type DB source isSort filter mode get
                // value/show in web getter parameters
                new FieldDescriptor(NAME, String.class.getName(), new DefaultDbSource(Relation.class, "name"), true, 1, BatchPresentationConsts.ASC,
                		FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.NONE, "name" }),
                new FieldDescriptor(DESCRIPTION, String.class.getName(), new DefaultDbSource(Relation.class, "description"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[] { Permission.NONE, "description" }) });
    }
}
