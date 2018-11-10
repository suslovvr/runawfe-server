package ru.runa.wfe.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.dom4j.Document;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

/**
 * Service for authorization.
 * 
 * @since 2.0
 */
public interface AuthorizationService {

    void checkAllowed(User user, Permission permission, SecuredObject securedObject);

    void checkAllowed(User user, Permission permission, SecuredObjectType type, Long id);

    /**
     * Checks whether user has permission on securedObject.
     */
    boolean isAllowed(User user, Permission permission, SecuredObject securedObject);

    /**
     * Checks whether user has permission on object.
     */
    boolean isAllowed(User user, Permission permission, SecuredObjectType type, Long id);

    /**
     * Checks whether user has permission on secured objects of the same secured object type.
     */
    <T extends SecuredObject> boolean[] isAllowed(User user, Permission permission, List<T> securedObjects);

    /**
     * Generally same as {@link #isAllowed(User, Permission, List)}, introduced to prevent creation of fake entities.
     */
    boolean[] isAllowed(User user, Permission permission, SecuredObjectType type, List<Long> ids);

    /**
     * Checks if user has parmission on any object of specified type.
     */
    boolean isAllowedForAny(User user, Permission permission, SecuredObjectType type);

    /**
     * Special case to check UPDATE and/or UPDATE_SELF permissions.
     */
    boolean isAllowedUpdateExecutor(User user, Executor object);

    /**
     * Special case to check UPDATE and/or UPDATE_SELF permissions.
     * Overloaded version for cases where otherwise getExecutor() would be called and perform extra READ check.
     */
    boolean isAllowedUpdateExecutor(User user, Long id);

    /**
     * Exports &lt;addPermissions&gt; elements to XML script. Everything is done under single transaction, using optimized queries.
     */
    void exportDataFile(User user, Document script);

    /**
     * Used by script's AddPermissionsOperation.
     */
    void addPermissions(User user, String executorName, Map<SecuredObjectType, Set<String>> objectNames, Set<Permission> permissions);

    /**
     * Used by script's RemovePermissionsOperation.
     */
    void removePermissions(User user, String executorName, Map<SecuredObjectType, Set<String>> objectNames, Set<Permission> permissions);

    /**
     * Used by script's RemoveAllPermissionsOperation.
     */
    void removeAllPermissions(User user, String executorName, Map<SecuredObjectType, Set<String>> objectNames);

    /**
     * Used by script's SetPermissionsOperation.
     */
    void setPermissions(User user, String executorName, Map<SecuredObjectType, Set<String>> objectNames, Set<Permission> permissions);

    /**
     * Sets permissions for executor specified by id on securedObject.
     */
    void setPermissions(User user, Long executorId, Collection<Permission> permissions, SecuredObject securedObject);

    /**
     * Sets permissions for executors specified by ids on securedObject.
     */
    void setPermissions(User user, List<Long> executorsId, List<Collection<Permission>> permissions, SecuredObject securedObject);

    /**
     * Sets permissions for executors specified by ids on securedObject.
     */
    void setPermissions(User user, List<Long> executorsId, Collection<Permission> permissions, SecuredObject securedObject);

    /**
     * Returns permissions that executor himself has on securedObject.
     * Permissions by privilege will not return.
     * 
     * @return Map of {Permission, Is permission can be modifiable}, not <code>null</code>
     */
    List<Permission> getIssuedPermissions(User user, Executor performer, SecuredObject securedObject);

    /**
     * Load executor's which already has (or not has) some permission on
     * specified securedObject. This query using paging.
     * 
     * @param user
     *            Current user {@linkplain User}.
     * @param securedObject
     *            {@linkplain SecuredObject} to load executors, which has (or
     *            not) permission on this securedObject.
     * @param batchPresentation
     *            {@linkplain BatchPresentation} for loading executors.
     * @param hasPermission
     *            Flag equals true to load executors with permissions on
     *            {@linkplain SecuredObject}; false to load executors without
     *            permissions.
     * @return Executors with or without permission on {@linkplain SecuredObject}
     */
    List<Executor> getExecutorsWithPermission(User user, SecuredObject securedObject, BatchPresentation batchPresentation, boolean hasPermission);

    /**
     * Load executor's count which already has (or not has) some permission on
     * specified securedObject.
     * 
     * @param user
     *            Current user {@linkplain User}.
     * @param securedObject
     *            {@linkplain SecuredObject} to load executors, which has (or
     *            not) permission on this securedObject.
     * @param batchPresentation
     *            {@linkplain BatchPresentation} for loading executors.
     * @param hasPermission
     *            Flag equals true to load executors with permissions on
     *            {@linkplain SecuredObject}; false to load executors without
     *            permissions.
     * @return Count of executors with or without permission on
     *         {@linkplain SecuredObject}.
     */
    int getExecutorsWithPermissionCount(User user, SecuredObject securedObject, BatchPresentation batchPresentation, boolean hasPermission);

    /**
     * Loads secured objects with permission filtering.
     */
    <T> List<T> getPersistentObjects(User user, BatchPresentation batchPresentation, Class<T> persistentClass,
                                                           Permission permission, SecuredObjectType[] securedObjectTypes, boolean enablePaging);

    SecuredObject findSecuredObject(SecuredObjectType type, Long id);
}
