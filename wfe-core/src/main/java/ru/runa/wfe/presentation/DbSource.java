package ru.runa.wfe.presentation;

/**
 * Interface for components, describes HQL statement to access stored field value.
 */
public abstract class DbSource {

    public enum AccessType {
        FILTER,
        ORDER
    }

    private final Class<?> sourceObject;

    DbSource(Class<?> sourceObject) {
        this.sourceObject = sourceObject;
    }

    /**
     * Source persistent object of this field. Alias to this object will be passed to other function. Persistent class will be added to HQL query then
     * required.
     * 
     * @return Persistent class of this field.
     */
    public final Class<?> getSourceObject() {
        return sourceObject;
    }

    /**
     * Returns HQL expression to access field value. Use alias parameter to access field persistent object (May be null, see parameter description).
     * For example to access id property with alias != null return 'alias.id' and with alias = null return 'id'.
     * 
     * @param accessType
     *            filter or order
     * @param alias
     *            Alias, assigned to field persistent class in HQL query. May be null, when return value must be HQL path to access value without root
     *            persistent class.
     * @return HQL expression to access field value. See alias parameter for more information.
     */
    public abstract String getValueDBPath(AccessType accessType, String alias);

    /**
     * How to join root {@link ClassPresentation} persistent object with field persistent object. If field is a property of root persistent object,
     * then return empty string. If field persistent object must be joined by id with root, then return: ClassPresentation.classNameSQL + ".id=" +
     * alias + ".id". If field must not be joined, but used for filtering, return simple alias + ".id"
     * 
     * @param alias
     *            Alias, assigned to field persistent class in HQL query.
     * @return HQL expression to join root persistent object with field persistent object.
     */
    public abstract String getJoinExpression(String alias);
}
