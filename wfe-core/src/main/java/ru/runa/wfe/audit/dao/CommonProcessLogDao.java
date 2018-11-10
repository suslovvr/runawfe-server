package ru.runa.wfe.audit.dao;

import java.util.List;
import org.hibernate.Query;
import ru.runa.wfe.audit.BaseProcessLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.Severity;
import ru.runa.wfe.commons.ApplicationContextFactory;

class CommonProcessLogDao {

    @SuppressWarnings("unchecked")
    static List<BaseProcessLog> getAll(final ProcessLogFilter filter, Class<? extends BaseProcessLog> entityClass) {
        boolean filterBySeverity = filter.getSeverities().size() != 0 && filter.getSeverities().size() != Severity.values().length;
        String hql = "from " + entityClass.getName() + " where processId = :processId";
        if (filter.getIdFrom() != null) {
            hql += " and id >= :idFrom";
        }
        if (filter.getIdTo() != null) {
            hql += " and id <= :idTo";
        }
        if (filter.getCreateDateFrom() != null) {
            hql += " and createDate >= :createDateFrom";
        }
        if (filter.getCreateDateTo() != null) {
            hql += " and createDate <= :createDateTo";
        }
        if (filter.getTokenId() != null) {
            hql += " and tokenId = :tokenId";
        }
        if (filter.getNodeId() != null) {
            hql += " and nodeId = :nodeId";
        }
        if (filterBySeverity) {
            hql += " and severity in (:severities)";
        }
        hql += " order by id asc";
        Query query = ApplicationContextFactory.getSessionFactory().getCurrentSession().createQuery(hql);
        query.setParameter("processId", filter.getProcessId());
        if (filter.getIdFrom() != null) {
            query.setParameter("idFrom", filter.getIdFrom());
        }
        if (filter.getIdTo() != null) {
            query.setParameter("idTo", filter.getIdTo());
        }
        if (filter.getCreateDateFrom() != null) {
            query.setParameter("createDateFrom", filter.getCreateDateFrom());
        }
        if (filter.getCreateDateTo() != null) {
            query.setParameter("createDateTo", filter.getCreateDateTo());
        }
        if (filter.getTokenId() != null) {
            query.setParameter("tokenId", filter.getTokenId());
        }
        if (filter.getNodeId() != null) {
            query.setParameter("nodeId", filter.getNodeId());
        }
        if (filterBySeverity) {
            query.setParameterList("severities", filter.getSeverities());
        }
        return query.list();
    }
}
