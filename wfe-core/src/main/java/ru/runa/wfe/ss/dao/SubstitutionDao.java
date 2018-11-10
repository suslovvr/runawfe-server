package ru.runa.wfe.ss.dao;

import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.ss.QSubstitution;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionDoesNotExistException;
import ru.runa.wfe.user.Actor;

/**
 * DAO level interface for managing {@linkplain Substitution}'s.
 * 
 * @since 2.0
 */
@Component
public class SubstitutionDao extends GenericDao<Substitution> {

    public SubstitutionDao() {
        super(Substitution.class);
    }

    @Override
    public Substitution create(Substitution entity) {
        entity.setCreateDate(new Date());
        return super.create(entity);
    }

    @Override
    protected void checkNotNull(Substitution entity, Object identity) {
        if (entity == null) {
            throw new SubstitutionDoesNotExistException(String.valueOf(identity));
        }
    }

    /**
     * Load {@linkplain Substitution}'s by identity. Result
     * {@linkplain Substitution}'s order is not specified.
     * 
     * @param ids
     *            {@linkplain Substitution}s identity to load.
     * @return Loaded {@linkplain Substitution}s.
     */
    public List<Substitution> get(List<Long> ids) {
        if (ids.isEmpty()) {
            return Lists.newArrayList();
        }
        val s = QSubstitution.substitution;
        return queryFactory.selectFrom(s).where(s.id.in(ids)).fetch();
    }

    /**
     * Loads all {@linkplain Substitution}'s for {@linkplain Actor}. Loaded
     * {@linkplain Substitution}'s is ordered by substitution position.
     * 
     * @param actorId
     *            {@linkplain Actor} identity to load {@linkplain Substitution}
     *            's.
     * @return {@linkplain Substitution}'s for {@linkplain Actor}.
     */
    public List<Substitution> getByActorId(Long actorId, boolean orderByPositionAscending) {
        val s = QSubstitution.substitution;
        return queryFactory.selectFrom(s)
                .where(s.actorId.eq(actorId)).orderBy(orderByPositionAscending ? s.position.asc() : s.position.desc())
                .fetch();
    }

    public void deleteAllActorSubstitutions(Long actorId) {
        List<Substitution> substitutions = getByActorId(actorId, true);
        for (Substitution substitution : substitutions) {
            delete(substitution);
        }
    }
}
