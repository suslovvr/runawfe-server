package ru.runa.wfe.audit.dao;

import org.springframework.stereotype.Component;
import ru.runa.wfe.audit.SystemLog;
import ru.runa.wfe.commons.dao.GenericDao;

/**
 * DAO level interface for managing {@linkplain SystemLog}.
 * 
 * @author Konstantinov Aleksey 25.02.2012
 */
@Component
public class SystemLogDao extends GenericDao<SystemLog> {

    public SystemLogDao() {
        super(SystemLog.class);
    }
}
