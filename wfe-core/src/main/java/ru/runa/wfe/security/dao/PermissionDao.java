package ru.runa.wfe.security.dao;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.mysema.commons.lang.CloseableIterator;
import com.querydsl.jpa.JPQLQuery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;
import lombok.val;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import ru.runa.wfe.commons.CollectionUtil;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.TimeMeasurer;
import ru.runa.wfe.commons.dao.CommonDao;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.hibernate.CompilerParameters;
import ru.runa.wfe.presentation.hibernate.PresentationCompiler;
import ru.runa.wfe.presentation.hibernate.RestrictionsToPermissions;
import ru.runa.wfe.security.ApplicablePermissions;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.PermissionSubstitutions;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.SecuredObjectUtil;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.dao.ExecutorDao;
import ru.runa.wfe.util.Pair;


/**
 * Permission DAO level implementation via Hibernate.
 *
 * @author Konstantinov Aleksey 19.02.2012
 */
@Component
@SuppressWarnings("unchecked")
public class PermissionDao extends CommonDao {

    private static final List<List<Long>> nonEmptyListList = Collections.singletonList(Collections.singletonList(1L));
    private static final Set<Long> nonEmptySet = Collections.singleton(1L);

    @Autowired
    private ExecutorDao executorDao;
    @Autowired
    private SessionFactory sessionFactory;

    private final Map<SecuredObjectType, Set<Executor>> privelegedExecutors = new HashMap<>();
    private final Set<Long> privelegedExecutorIds = new HashSet<>();

    public PermissionDao() {
        for (SecuredObjectType type : SecuredObjectType.values()) {
            privelegedExecutors.put(type, new HashSet<>());
        }
    }

    /**
     * Called once after migrations are successfully applied.
     */
    public void preloadPrivilegedMapping() {
        val pm = QPrivelegedMapping.privelegedMapping;
        CloseableIterator<PrivelegedMapping> i = queryFactory.selectFrom(pm).iterate();
        while (i.hasNext()) {
            PrivelegedMapping m = i.next();
            privelegedExecutors.get(m.getType()).add(m.getExecutor());
            privelegedExecutorIds.add(m.getExecutor().getId());
        }
        i.close();
    }

    public List<Permission> getIssuedPermissions(Executor executor, SecuredObject object) {
        val pm = QPermissionMapping.permissionMapping;
        return queryFactory.select(pm.permission).from(pm)
                .where(pm.objectType.eq(object.getSecuredObjectType())
                        .and(pm.objectId.eq(object.getIdentifiableId()))
                        .and(pm.executor.eq(executor)))
                .fetch();
    }

    /**
     * Sets permissions for executor on securedObject.
     *
     * @param executor
     *            Executor, which got permissions.
     * @param permissions
     *            Permissions for executor.
     * @param object
     *            Secured object to set permission on.
     */
    public void setPermissions(Executor executor, Collection<Permission> permissions, SecuredObject object) {
        ApplicablePermissions.check(object, permissions);
        if (isPrivilegedExecutor(object, executor)) {
            logger.debug(permissions + " not granted for privileged " + executor);
            return;
        }

        List<Permission> issued = getIssuedPermissions(executor, object);
        Set<Permission> toAdd = new HashSet<>(permissions);
        toAdd.removeAll(issued);
        Set<Permission> toDelete = new HashSet<>(issued);
        toDelete.removeAll(permissions);

        if (!toAdd.isEmpty()) {
            Session session = sessionFactory.getCurrentSession();
            for (Permission p : toAdd) {
                session.save(new PermissionMapping(executor, object, p));
            }
        }
        if (!toDelete.isEmpty()) {
            val pm = QPermissionMapping.permissionMapping;
            queryFactory.delete(pm)
                    .where(pm.objectType.eq(object.getSecuredObjectType())
                            .and(pm.objectId.eq(object.getIdentifiableId()))
                            .and(pm.executor.eq(executor))
                            .and(pm.permission.in(toDelete)))
                    .execute();
        }
    }

    /**
     * Throws if user has no permission to object.
     */
    public void checkAllowed(User user, Permission permission, SecuredObject object) {
        if (!isAllowed(user, permission, object)) {
            throw new AuthorizationException(user + " does not have " + permission + " to " + object);
        }
    }

    /**
     * Throws if user has no permission to {type, id}.
     */
    public void checkAllowed(User user, Permission permission, SecuredObjectType type, Long id) {
        if (!isAllowed(user, permission, type, id)) {
            throw new AuthorizationException(user + " does not have " + permission + " to (" + type + ", " + id + ")");
        }
    }

    /**
     * Throws if user has no permission to {type, all given ids}.
     */
    public void checkAllowedForAll(User user, Permission permission, SecuredObjectType type, List<Long> ids) {
        Assert.notNull(ids);
        List<Long> notAllowed = CollectionUtil.diffList(ids, filterAllowedIds(user.getActor(), permission, type, ids));
        if (!notAllowed.isEmpty()) {
            Collections.sort(notAllowed);
            throw new AuthorizationException("User " + user + " does not have " + permission + " on all of (" + type + ", " + notAllowed + ")");
        }
    }

    /**
     * Returns true if user have permission to object.
     */
    public boolean isAllowed(User user, Permission permission, SecuredObject object) {
        return isAllowed(user.getActor(), permission, object.getSecuredObjectType(), object.getIdentifiableId());
    }

    public boolean isAllowed(User user, Permission permission, SecuredObjectType type, Long id) {
        return isAllowed(user.getActor(), permission, type, id);
    }

    public boolean isAllowed(Executor executor, Permission permission, SecuredObjectType type, Long id) {
        Assert.notNull(id);
        return !filterAllowedIds(executor, permission, type, Collections.singletonList(id)).isEmpty();
    }

    public boolean isAllowed(Executor executor, Permission permission, SecuredObject object, boolean checkPrivileged) {
        Long id = object.getIdentifiableId();
        SecuredObjectType type = object.getSecuredObjectType();
        Assert.notNull(id);
        return !filterAllowedIds(executor, permission, type, Collections.singletonList(id), checkPrivileged).isEmpty();
    }

    /**
     * Returns true if user have permission to {type, any id}.
     */
    public boolean isAllowedForAny(User user, Permission permission, SecuredObjectType type) {
        return !filterAllowedIds(user.getActor(), permission, type, null).isEmpty();
    }

    public Set<Long> filterAllowedIds(Executor executor, Permission permission, SecuredObjectType type, List<Long> idsOrNull) {
        return filterAllowedIds(executor, permission, type, idsOrNull, true);
    }

    /**
     * Returns subset of `idsOrNull` for which `actor` has `permission`. If `idsOrNull` is null (e.g. when called from isAllowedForAny()),
     * non-empty set (containing arbitrary value) means positive check result.
     *
     * @param checkPrivileged If false, only permission_mapping table is checked, but not privileged_mapping.
     */
    public Set<Long> filterAllowedIds(Executor executor, Permission permission, SecuredObjectType type, List<Long> idsOrNull, boolean checkPrivileged) {
        ApplicablePermissions.check(type, permission);
        boolean haveIds = idsOrNull != null;

        if (permission == Permission.NONE) {
            // Optimization; see comments at NONE definition.
            return Collections.emptySet();
        }

        final Set<Executor> executorWithGroups = getExecutorWithAllHisGroups(executor);
        if (checkPrivileged && isPrivilegedExecutor(type, executorWithGroups)) {
            return haveIds ? new HashSet<>(idsOrNull) : nonEmptySet;
        }

        PermissionSubstitutions.ForCheck subst = PermissionSubstitutions.getForCheck(type, permission);
        val pm = QPermissionMapping.permissionMapping;

        // Same type for all objects, thus same listType. I believe it would be faster to perform separate query here.
        // ATTENTION!!! Also, HQL query with two conditions (on both type and listType) always returns empty rowset. :(
        //              (Both here with QueryDSL and in HibernateCompilerHQLBuilder.addSecureCheck() with raw HQL.)
        if (!subst.listPermissions.isEmpty() && queryFactory.select(pm.id).from(pm)
                .where(pm.executor.in(executorWithGroups)
                        .and(pm.objectType.eq(type.getListType()))
                        .and(pm.objectId.eq(0L))
                        .and(pm.permission.in(subst.listPermissions)))
                .fetchFirst() != null) {
            return haveIds ? new HashSet<>(idsOrNull) : nonEmptySet;
        }

        val result = new HashSet<Long>();
        val typesToCheck = new HashSet<SecuredObjectType>();
        typesToCheck.add(type);
        if (type == SecuredObjectType.ACTOR || type == SecuredObjectType.GROUP) {
            typesToCheck.add(SecuredObjectType.EXECUTOR);
        }
        for (List<Long> idsPart : haveIds ? Lists.partition(idsOrNull, SystemProperties.getDatabaseParametersCount()) : nonEmptyListList) {
            JPQLQuery<Long> q = queryFactory.select(pm.id).from(pm)
                    .where(pm.executor.in(executorWithGroups)
                            .and(pm.objectType.in(typesToCheck))
                            .and(pm.permission.in(subst.selfPermissions)));
            if (haveIds) {
                result.addAll(q.where(pm.objectId.in(idsPart)).fetch());
            } else if (q.fetchFirst() != null) {
                return nonEmptySet;
            }
        }
        return result;
    }

    /**
     * Checks whether executor has permission on securedObject's. Create result array in same order, as securedObject's.
     *
     * TODO Merge with filterAllowedIds() method above, by returning BOTH results. (But what about "ids" here vs "idsOrNull" there?)
     *
     * @param user
     *            Executor, which permission must be check.
     * @param permission
     *            Checking permission.
     * @param ids
     *            Secured object IDs to check.
     * @return Array of: true if executor has requested permission on securedObject; false otherwise.
     */
    public boolean[] isAllowed(User user, Permission permission, SecuredObjectType type, List<Long> ids) {
        boolean[] result = new boolean[ids.size()];
        if (result.length == 0) {
            return result;
        }

        if (permission == Permission.NONE) {
            // Optimization; see comments at NONE definition.
            Arrays.fill(result, false);
            return result;
        }

        Set<Executor> executorWithGroups = getExecutorWithAllHisGroups(user.getActor());
        if (isPrivilegedExecutor(type, executorWithGroups)) {
            Arrays.fill(result, true);
            return result;
        }

        PermissionSubstitutions.ForCheck subst = PermissionSubstitutions.getForCheck(type, permission);
        val pm = QPermissionMapping.permissionMapping;
        // Same type for all objects, thus same listType. I believe it would be faster to perform separate query here.
        if (!subst.listPermissions.isEmpty() && queryFactory.select(pm.id).from(pm)
                .where(pm.executor.in(executorWithGroups)
                        .and(pm.objectType.eq(type.getListType()))
                        .and(pm.objectId.eq(0L))
                        .and(pm.permission.in(subst.listPermissions)))
                .fetchFirst() != null) {
            Arrays.fill(result, true);
            return result;
        }

        val typesToCheck = new HashSet<SecuredObjectType>();
        typesToCheck.add(type);
        if (type == SecuredObjectType.ACTOR || type == SecuredObjectType.GROUP) {
            typesToCheck.add(SecuredObjectType.EXECUTOR);
        }
        val allowedIdentifiableIds = new HashSet<Long>(result.length);
        int window = SystemProperties.getDatabaseParametersCount() - executorWithGroups.size() - 2;
        Preconditions.checkArgument(window > 100);
        for (int i = 0; i <= (result.length - 1) / window; ++i) {
            int start = i * window;
            int end = Math.min((i + 1) * window, result.length);
            List<Long> identifiableIds = new ArrayList<>(end - start);
            for (int j = start; j < end; j++) {
                identifiableIds.add(ids.get(j));
            }
            if (identifiableIds.isEmpty()) {
                break;
            }
            allowedIdentifiableIds.addAll(queryFactory.selectDistinct(pm.objectId).from(pm)
                    .where(pm.executor.in(executorWithGroups)
                            .and(pm.objectType.in(typesToCheck))
                            .and(pm.objectId.in(identifiableIds))
                            .and(pm.permission.in(subst.selfPermissions)))
                    .fetch());
        }
        for (int i = 0; i < ids.size(); i++) {
            result[i] = allowedIdentifiableIds.contains(ids.get(i));
        }
        return result;
    }

    public <T extends SecuredObject> boolean[] isAllowed(User user, Permission permission, List<T> objects) {
        Pair<SecuredObjectType, ArrayList<Long>> pair = SecuredObjectUtil.splitObjectsToTypeAndIds(objects);
        return isAllowed(user, permission, pair.getValue1(), pair.getValue2());
    }

    private Set<Executor> getExecutorWithAllHisGroups(Executor executor) {
        Set<Executor> set = new HashSet<>(executorDao.getExecutorParentsAll(executor, false));
        set.add(executor);
        return set;
    }

    /**
     * Deletes all permissions for executor.
     */
    public void deleteOwnPermissions(Executor executor) {
        val pm = QPermissionMapping.permissionMapping;
        queryFactory.delete(pm).where(pm.executor.eq(executor)).execute();
    }

    /**
     * Deletes all permissions for securedObject.
     */
    public void deleteAllPermissions(@NonNull SecuredObject obj) {
        deleteAllPermissions(obj.getSecuredObjectType(), obj.getIdentifiableId());
    }

    public void deleteAllPermissions(@NonNull SecuredObjectType type, long id) {
        QPermissionMapping pm = QPermissionMapping.permissionMapping;
        queryFactory.delete(pm).where(pm.objectType.eq(type).and(pm.objectId.eq(id))).execute();
    }

    /**
     * Load {@linkplain Executor}s which have permission on {@linkplain SecuredObject}. <b>Paging is not enabled.</b>
     */
    public Set<Executor> getExecutorsWithPermission(SecuredObject obj) {
        val pm = QPermissionMapping.permissionMapping;
        List<Executor> list = queryFactory.selectDistinct(pm.executor).from(pm)
                .where(pm.objectType.eq(obj.getSecuredObjectType()).and(pm.objectId.eq(obj.getIdentifiableId())))
                .fetch();
        Set<Executor> result = new HashSet<>(list);
        result.addAll(getPrivilegedExecutors(obj.getSecuredObjectType()));
        return result;
    }

    /**
     * Return array of privileged {@linkplain Executor}s for given (@linkplain SecuredObject) type (i.e. executors whose permissions on SecuredObject
     * type can not be changed).
     *
     * @return Privileged {@linkplain Executor}'s array.
     */
    public Collection<Executor> getPrivilegedExecutors(SecuredObjectType securedObjectType) {
        return privelegedExecutors.get(securedObjectType);
    }

    /**
     * Check if executor is privileged executor for any secured object type.
     */
    public boolean isPrivilegedExecutor(Executor executor) {
        for (Set<Executor> executors : privelegedExecutors.values()) {
            if (executors.contains(executor)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if executor is privileged executor for any secured object type.
     */
    public boolean hasPrivilegedExecutor(List<Long> executorIds) {
        for (Long executorId : executorIds) {
            if (privelegedExecutorIds.contains(executorId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if executor is privileged executor for given object.
     *
     * @param executor
     *            {@linkplain Executor}, to check if privileged.
     * @param object
     *            {@linkplain SecuredObject} object, to check if executor is privileged to it.
     * @return true if executor is privileged for given object and false otherwise.
     */
    private boolean isPrivilegedExecutor(SecuredObject object, Executor executor) {
        Collection<Executor> executorWithGroups = getExecutorWithAllHisGroups(executor);
        return isPrivilegedExecutor(object.getSecuredObjectType(), executorWithGroups);
    }

    private boolean isPrivilegedExecutor(SecuredObjectType type, Collection<Executor> executorWithGroups) {
        for (Executor executor : executorWithGroups) {
            if (getPrivilegedExecutors(type).contains(executor)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds new record in <i>dictionary</i> tables describing new SecuredObject type.
     *
     * @param type
     *            Type of SecuredObject.
     * @param executors
     *            Privileged executors for target class.
     */
    public void addType(SecuredObjectType type, List<? extends Executor> executors) {
        for (Executor executor : executors) {
            PrivelegedMapping mapping = new PrivelegedMapping(type, executor);
            sessionFactory.getCurrentSession().save(mapping);
            privelegedExecutors.get(mapping.getType()).add(mapping.getExecutor());
            privelegedExecutorIds.add(mapping.getExecutor().getId());
        }
    }

    /**
     * Load list of {@linkplain SecuredObject} for which executors have permission on.
     *
     * @param user
     *            User which must have permission on loaded {@linkplain SecuredObject} (at least one).
     * @param batchPresentation
     *            {@linkplain BatchPresentation} with parameters for loading {@linkplain SecuredObject}'s.
     * @param permission
     *            {@linkplain Permission}, which executors must has on {@linkplain SecuredObject}.
     * @param securedObjectTypes
     *            {@linkplain SecuredObjectType} types, used to check permissions.
     * @param enablePaging
     *            Flag, equals true, if paging must be enabled and false otherwise.
     * @return List of {@link SecuredObject}'s for which executors have permission on.
     */
    public List<? extends SecuredObject> getPersistentObjects(User user, BatchPresentation batchPresentation, Permission permission,
            SecuredObjectType[] securedObjectTypes, boolean enablePaging) {
        TimeMeasurer timeMeasurer = new TimeMeasurer(logger, 1000);
        timeMeasurer.jobStarted();
        RestrictionsToPermissions permissions = new RestrictionsToPermissions(user, permission, securedObjectTypes);
        CompilerParameters parameters = CompilerParameters.create(enablePaging).addPermissions(permissions);
        List<? extends SecuredObject> result = new PresentationCompiler(batchPresentation).getBatch(parameters);
        timeMeasurer.jobEnded("getObjects: " + result.size());
        if (result.size() == 0 && enablePaging && batchPresentation.getPageNumber() > 1) {
            logger.debug("resetting batch presentation to first page due to 0 results");
            batchPresentation.setPageNumber(1);
            result = getPersistentObjects(user, batchPresentation, permission, securedObjectTypes, enablePaging);
        }
        return result;
    }

    /**
     * Load count of {@linkplain SecuredObject} for which executors have permission on.
     *
     * @param user
     *            User which must have permission on loaded {@linkplain SecuredObject} (at least one).
     * @param batchPresentation
     *            {@linkplain BatchPresentation} with parameters for loading {@linkplain SecuredObject}'s.
     * @param permission
     *            {@linkplain Permission}, which executors must have on {@linkplain SecuredObject}.
     * @param securedObjectTypes
     *            {@linkplain SecuredObjectType} types, used to check permissions.
     * @return Count of {@link SecuredObject}'s for which executors have permission on.
     */
    public int getPersistentObjectCount(User user, BatchPresentation batchPresentation, Permission permission, SecuredObjectType[] securedObjectTypes) {
        TimeMeasurer timeMeasurer = new TimeMeasurer(logger, 1000);
        timeMeasurer.jobStarted();
        RestrictionsToPermissions permissions = new RestrictionsToPermissions(user, permission, securedObjectTypes);
        CompilerParameters parameters = CompilerParameters.createNonPaged().addPermissions(permissions);
        int count = new PresentationCompiler(batchPresentation).getCount(parameters);
        timeMeasurer.jobEnded("getCount: " + count);
        return count;
    }

    public boolean permissionExists(final Executor executor, final Permission permission, final SecuredObject object) {
        val pm = QPermissionMapping.permissionMapping;
        return queryFactory.select(pm.id).from(pm)
                .where(pm.executor.eq(executor)
                        .and(pm.objectType.eq(object.getSecuredObjectType()))
                        .and(pm.objectId.eq(object.getIdentifiableId()))
                        .and(pm.permission.eq(permission)))
                .fetchFirst() != null;
    }
}
