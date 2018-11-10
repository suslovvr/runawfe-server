package ru.runa.wfe.relation.dao;

import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.relation.QRelation;
import ru.runa.wfe.relation.QRelationPair;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationAlreadyExistException;
import ru.runa.wfe.relation.RelationDoesNotExistException;

/**
 * Relation dao implementation via Hibernate.
 * 
 * @author Konstantinov Aleksey 12.02.2012
 * @since 3.3
 */
@Component
public class RelationDao extends GenericDao<Relation> {

    public RelationDao() {
        super(Relation.class);
    }

    @Override
    protected void checkNotNull(Relation entity, Object identity) {
        if (entity == null) {
            throw new RelationDoesNotExistException(identity);
        }
    }

    @Override
    public Relation create(Relation relation) {
        if (get(relation.getName()) != null) {
            throw new RelationAlreadyExistException(relation.getName());
        }
        return super.create(relation);
    }

    /**
     * Return {@link Relation} with specified name or throws
     * {@link RelationDoesNotExistException} if relation with such name does not
     * exists.
     * 
     * @param name
     *            Relation name
     * @return Relation with specified name.
     */
    public Relation getNotNull(String name) {
        Relation relation = get(name);
        checkNotNull(relation, name);
        return relation;
    }

    public Relation get(String name) {
        val r = QRelation.relation;
        return queryFactory.selectFrom(r).where(r.name.eq(name)).fetchFirst();
    }

    @Override
    public void delete(Long id) {
        val rp = QRelationPair.relationPair;
        queryFactory.delete(rp).where(rp.relation.id.eq(id)).execute();
        super.delete(id);
    }
}
