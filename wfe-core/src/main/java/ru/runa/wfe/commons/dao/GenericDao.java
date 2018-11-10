package ru.runa.wfe.commons.dao;

import com.google.common.base.Preconditions;
import java.util.List;

/**
 * General DAO implementation (type-safe generic DAO pattern).
 * 
 * @author dofs
 * @since 4.0
 * 
 * @param <T>
 *            entity class
 */
@SuppressWarnings("unchecked")
public abstract class GenericDao<T> extends ReadOnlyGenericDao<T> {

    /**
     * Default constructor fails to determine entityClass if subclass is generic:
     * in this case, getActualTypeArguments() returns TypeVariableImpl instead of Class.
     */
    public GenericDao(Class<T> entityClass) {
        super(entityClass);
    }

    @Override
    public T get(Long id) {
        Preconditions.checkArgument(id != null);
        return get(entityClass, id);
    }

    /**
     * Load entity from database by id.
     * 
     * @return entity.
     */
    public T getNotNull(Long id) {
        T entity = get(id);
        checkNotNull(entity, id);
        return entity;
    }

    /**
     * Checks that entity is not null. Throws exception in that case. Used in *NotNull methods. Expected to be overriden in subclasses.
     * 
     * @param entity
     *            test entity
     * @param identity
     *            search data
     */
    protected void checkNotNull(T entity, Object identity) {
        if (entity == null) {
            throw new NullPointerException("No entity found");
        }
    }

    /**
     * Load all entities from database.
     * 
     * @return entities list, not <code>null</code>.
     */
    public List<T> getAll() {
        return sessionFactory.getCurrentSession().createQuery("from " + entityClass.getName()).list();
    }

    /**
     * Saves transient entity.
     * 
     * @return saved entity.
     */
    public T create(T entity) {
        sessionFactory.getCurrentSession().save(entity);
        return entity;
    }

    /**
     * Updates entity.
     * 
     * @param entity
     *            detached entity
     */
    public T update(T entity) {
        return (T)sessionFactory.getCurrentSession().merge(entity);
    }

    /**
     * Flush all pending saves, updates and deletes to the database.
     */
    public void flushPendingChanges() {
        // TODO flush?
        sessionFactory.getCurrentSession().flush();
    }

    /**
     * Deletes entity from DB by id.
     */
    public void delete(Long id) {
        Preconditions.checkNotNull(id);
        delete(getNotNull(id));
    }

    /**
     * Deletes entities from DB by ids.
     */
    public void delete(List<Long> ids) {
        Preconditions.checkNotNull(ids);
        for (Long id : ids) {
            delete(id);
        }
    }

    /**
     * Deletes entity from DB
     */
    public void delete(T entity) {
        Preconditions.checkNotNull(entity);
        sessionFactory.getCurrentSession().delete(entity);
    }
}
