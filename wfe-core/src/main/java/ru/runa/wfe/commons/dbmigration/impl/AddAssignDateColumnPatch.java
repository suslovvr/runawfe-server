package ru.runa.wfe.commons.dbmigration.impl;

import com.google.common.base.Objects;
import java.sql.Types;
import java.util.List;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.audit.BaseProcessLog;
import ru.runa.wfe.audit.CurrentTaskAssignLog;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.TaskAssignLog;
import ru.runa.wfe.audit.dao.CurrentProcessLogDao;
import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddAssignDateColumnPatch extends DbMigration {
    @Autowired
    private CurrentProcessLogDao currentProcessLogDao;

    @Override
    protected void executeDDLBefore() {
        executeUpdates(getDDLCreateColumn("BPM_TASK", new ColumnDef("ASSIGN_DATE", dialect.getTypeName(Types.TIMESTAMP))));
    }

    @Override
    public void executeDML(Session session) {
        List<Object[]> rows = session.createSQLQuery("SELECT ID, PROCESS_ID, NODE_ID FROM BPM_TASK").list();
        log.info("Found " + rows.size() + " tasks");
        SQLQuery updateQuery = session.createSQLQuery("UPDATE BPM_TASK SET ASSIGN_DATE=:assignDate WHERE ID=:taskId");
        for (Object[] row : rows) {
            Long taskId = ((Number) row[0]).longValue();
            ProcessLogFilter filter = new ProcessLogFilter(((Number) row[1]).longValue());
            filter.setType(ProcessLog.Type.TASK_ASSIGN);
            filter.setNodeId((String) row[2]);
            List<BaseProcessLog> logs = currentProcessLogDao.getAll(filter);
            for (BaseProcessLog processLog : logs) {
                TaskAssignLog taskAssignLog = (CurrentTaskAssignLog) processLog;
                if (Objects.equal(taskId, taskAssignLog.getTaskId())) {
                    updateQuery.setParameter("assignDate", taskAssignLog.getCreateDate());
                    updateQuery.setParameter("taskId", taskId);
                    updateQuery.executeUpdate();
                    break;
                }
            }
        }
    }
}
