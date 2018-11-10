package ru.runa.wfe.presentation;

import com.google.common.base.Objects;
import java.sql.Date;
import java.util.Arrays;
import java.util.Calendar;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.presentation.filter.FilterCriteria;

/**
 * Description for field, available via {@link ClassPresentation}. Contains almost all aspects of field behavior.
 */
@CommonsLog
public class FieldDescriptor {

    /**
     * Struts property, which will be used to get field display name.<br/>
     * ATTENTION: If this field contains {@link ClassPresentation} editable or removable prefix, it must be treated in special way (see
     * {@link ClassPresentation} for more details).
     */
    public final String displayName;

    /**
     * Field type as class name (i. e. String.class.getName()). Used to get appreciate {@link FilterCriteria} and FilterTDFormatter (see web project).
     * So, filter representation is depends on this field. {@link Calendar} will be created for {@link Date}, editor field for String and so on.
     */
    public final String fieldType;

    /**
     * Flag, equals true, if this field visible by default; false otherwise.
     */
    private boolean visible = true;

    /**
     * Flag, equals true, if this field can be grouped or sorted; false otherwise.
     */
    public final boolean sortable;

    /**
     * Flag, equals true, if this field can be showed; false otherwise.
     */
    public boolean showable = true;

    /**
     * The sort order, if the field is used for default batch sorting. Sorted fields indexes must start with 1 and be exactly sequential. Are set to
     * -1 if not participate in default sorting.
     */
    public final int defaultSortOrder;
    private static final int notUsedSortOrder = -1;

    /**
     * The sort mode if the field is used for default batch sorting. BatchPresentationConsts.ASC or BatchPresentationConsts.DSC considered in place.
     */
    public final boolean defaultSortMode;

    /**
     * Field filter mode.
     */
    public final FieldFilterMode filterMode;

    /**
     * Preferred way to get value of this field and show this field in web interface. (Class name)
     */
    public final String tdBuilder;

    /**
     * Parameters, passed to tdBuilder constructor.
     */
    public final Object[] tdBuilderParams;

    /**
     * Components, to access field values from HQL/SQL. If more then one components supplied, then first component must describe access to base class
     * and other components must describe access to inherited objects.
     */
    public final DbSource[] dbSources;

    /**
     * Ordinal field index in {@link BatchPresentation}. All fields in {@link ClassPresentation} has -1, but {@link BatchPresentation} creates fields
     * with indexes using createConcretteField.
     */
    public final int fieldIdx;

    /**
     * Field display and HQL/SQL affecting state.
     */
    public final FieldState fieldState;

    /**
     * Creates field description.
     *
     * @param displayName
     *            Struts property, which will be used to get field display name.
     * @param fieldType
     *            Field type as class name (i. e. String.class.getName()).
     * @param dbSource
     *            Components, to access field values from HQL/SQL.
     * @param sortable
     *            Flag, equals true, if this field can be grouped or sorted; false otherwise.
     * @param filterMode
     *            Field filter mode.
     * @param fieldState
     *            Field display and HQL/SQL affecting state.
     */
    public FieldDescriptor(String displayName, String fieldType, DbSource dbSource, boolean sortable, FieldFilterMode filterMode,
            FieldState fieldState) {
        this(displayName, fieldType, new DbSource[] { dbSource }, sortable, notUsedSortOrder, BatchPresentationConsts.ASC, filterMode, null, null,
                -1, fieldState);
    }

    /**
     * Creates field description.
     *
     * @param displayName
     *            Struts property, which will be used to get field display name.
     * @param fieldType
     *            Field type as class name (i. e. String.class.getName()).
     * @param dbSource
     *            Component, to access field values from HQL/SQL.
     * @param sortable
     *            Flag, equals true, if this field can be grouped or sorted; false otherwise.
     * @param filterMode
     *            Field filter mode.
     * @param tdBuilder
     *            Preferred way to get value of this field and show this field in web interface. (Class name)
     * @param tdBuilderParams
     *            Parameters, passed to tdBuilder constructor.
     */
    public FieldDescriptor(String displayName, String fieldType, DbSource dbSource, boolean sortable, FieldFilterMode filterMode, String tdBuilder,
            Object[] tdBuilderParams) {
        this(displayName, fieldType, new DbSource[] { dbSource }, sortable, notUsedSortOrder, BatchPresentationConsts.ASC, filterMode, tdBuilder,
                tdBuilderParams, -1, null);
    }

    /**
     * Creates field description.
     *
     * @param displayName
     *            Struts property, which will be used to get field display name.
     * @param fieldType
     *            Field type as class name (i. e. String.class.getName()).
     * @param dbSource
     *            Component, to access field values from HQL/SQL.
     * @param sortable
     *            Flag, equals true, if this field can be grouped or sorted; false otherwise.
     * @param defaultSortOrder
     *            The sort order, if the field is used for default batch sorting.
     * @param defaultSortMode
     *            The sort mode, if the field is used for default batch sorting.
     * @param filterMode
     *            Field filter mode.
     * @param tdBuilder
     *            Preferred way to get value of this field and show this field in web interface. (Class name)
     * @param tdBuilderParams
     *            Parameters, passed to tdBuilder constructor.
     */
    public FieldDescriptor(String displayName, String fieldType, DbSource dbSource, boolean sortable, int defaultSortOrder, boolean defaultSortMode,
            FieldFilterMode filterMode, String tdBuilder, Object[] tdBuilderParams) {
        this(displayName, fieldType, new DbSource[] { dbSource }, sortable, defaultSortOrder, defaultSortMode, filterMode, tdBuilder,
                tdBuilderParams, -1, null);
    }

    public FieldDescriptor(String displayName, String fieldType, DbSource[] dbSources, boolean sortable, FieldFilterMode filterMode,
            String tdBuilder, Object[] tdBuilderParams) {
        this(displayName, fieldType, dbSources, sortable, notUsedSortOrder, BatchPresentationConsts.ASC, filterMode, tdBuilder, tdBuilderParams, -1,
                null);
    }

    /**
     * Creates field description.
     *
     * @param displayName
     *            Struts property, which will be used to get field display name.
     * @param fieldType
     *            Field type as class name (i. e. String.class.getName()).
     * @param dbSources
     *            Components, to access field values from HQL/SQL.
     * @param sortable
     *            Flag, equals true, if this field can be grouped or sorted; false otherwise.
     * @param filterMode
     *            Field filter mode.
     * @param tdBuilder
     *            Preferred way to get value of this field and show this field in web interface. (Class name)
     * @param tdBuilderParams
     *            Parameters, passed to tdBuilder constructor.
     * @param fieldIdx
     *            Ordinal field index in {@link BatchPresentation}.
     * @param fieldState
     *            Field display and HQL/SQL affecting state.
     */
    private FieldDescriptor(String displayName, String fieldType, DbSource[] dbSources, boolean sortable, int defaultSortOrder,
            boolean defaultSortMode, FieldFilterMode filterMode, String tdBuilder, Object[] tdBuilderParams, int fieldIdx, FieldState fieldState) {
        this.displayName = displayName;
        this.fieldType = fieldType;
        this.sortable = sortable;
        this.defaultSortOrder = defaultSortOrder;
        this.defaultSortMode = defaultSortMode;
        this.filterMode = filterMode;
        this.tdBuilder = tdBuilder;
        this.tdBuilderParams = tdBuilderParams;
        this.dbSources = dbSources;
        this.fieldIdx = fieldIdx;
        this.fieldState = fieldState == null ? loadFieldState(displayName) : fieldState;
        if (filterMode == FieldFilterMode.DATABASE_ID_RESTRICTION && sortable) {
            throw new InternalApplicationException("DATABASE_ID_RESTRICTION must not be used on filterable fields.");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FieldDescriptor)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        FieldDescriptor other = (FieldDescriptor) obj;
        return Objects.equal(displayName, other.displayName) && Objects.equal(fieldType, other.fieldType)
                && Arrays.equals(dbSources, other.dbSources);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(displayName);
    }

    /**
     * Creates {@link FieldDescriptor} instance with same parameters as current, but with provided field index.
     *
     * @param fieldIdx
     *            Index, assigned to field.
     * @return {@link FieldDescriptor} instance with provided index.
     */
    public FieldDescriptor createConcreteField(int fieldIdx) {
        return new FieldDescriptor(displayName, fieldType, dbSources, sortable, defaultSortOrder, defaultSortMode, filterMode, tdBuilder,
                tdBuilderParams, fieldIdx, fieldState).setVisible(visible).setShowable(showable);
    }

    /**
     * Creates removable field for editable field. If this method called not to editable field, null will be returned.
     *
     * @param value
     *            Value, inserted by user to editable field editor.
     * @param fieldIdx
     *            New removable field index.
     * @return {@link FieldDescriptor} for removable field, constructed based on editable field.
     */
    public FieldDescriptor createConcreteEditableField(String value, int fieldIdx) {
        if (!displayName.startsWith(ClassPresentation.editable_prefix)) {
            throw new InternalApplicationException("Field '" + displayName + "' is not editable");
        }
        return new FieldDescriptor(displayName.replace(ClassPresentation.editable_prefix, ClassPresentation.removable_prefix) + ":" + value,
                fieldType, dbSources, sortable, defaultSortOrder, defaultSortMode, filterMode, tdBuilder, tdBuilderParams, fieldIdx, fieldState);
    }

    private Object loadedTdBuilder;

    /**
     * Returns preferred object to display this field value in web interface.
     *
     * @return TdBuilder instance.
     */
    public Object getTdBuilder() {
        if (loadedTdBuilder == null) {
            loadedTdBuilder = loadTdBuilder();
        }
        return loadedTdBuilder;
    }

    public boolean isVisible() {
        return visible;
    }

    public FieldDescriptor setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    public boolean isShowable() {
        return showable;
    }

    public FieldDescriptor setShowable(boolean showable) {
        this.showable = showable;
        return this;
    }

    /**
     * Loads preferred object to display this field value in web interface.
     *
     * @return TdBuilder instance.
     */
    private Object loadTdBuilder() {
        Object builder = null;
        if (displayName.startsWith(ClassPresentation.removable_prefix)) {
            Object[] params = new Object[tdBuilderParams.length + 1];
            for (int idx = 0; idx < tdBuilderParams.length; ++idx) {
                params[idx] = tdBuilderParams[idx];
            }
            params[params.length - 1] = displayName.substring(displayName.lastIndexOf(':') + 1);
            builder = ClassLoaderUtil.instantiate(tdBuilder, params);
        } else if (displayName.startsWith(ClassPresentation.editable_prefix)) {
            Object[] params = new Object[tdBuilderParams.length + 1];
            for (int idx = 0; idx < tdBuilderParams.length; ++idx) {
                params[idx] = tdBuilderParams[idx];
            }
            params[params.length - 1] = "";
            builder = ClassLoaderUtil.instantiate(tdBuilder, params);
        } else {
            builder = ClassLoaderUtil.instantiate(tdBuilder, tdBuilderParams);
        }
        return builder;
    }

    /**
     * Load field state from properties file. If property loading fails, return ENABLED.
     *
     * @param displayName
     *            Field display name.
     * @return Field state, loaded from properties file.
     */
    private FieldState loadFieldState(String displayName) {
        try {
            return ClassPresentationResources.getFieldState(displayName);
        } catch (Exception e) {
            log.warn("Can't load state for field " + displayName, e);
        }
        return FieldState.ENABLED;
    }
}
