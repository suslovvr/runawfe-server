package ru.runa.wfe.job.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.apachecommons.CommonsLog;
import lombok.val;
import org.apache.commons.lang.StringUtils;
import org.hibernate.dialect.Dialect;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.DbType;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.hibernate.HibernateUtil;
import ru.runa.wfe.execution.NodeProcess;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.extension.ProcessArchiverStepHandler;
import ru.runa.wfe.var.Variable;

@CommonsLog
public class ProcessArchiver {
    /**
     * ATTENTION! List of ALL process IDs is generated from this number of ROOT process ids and then used in all SQL statements.
     * So if you specify too much, and if your process hierarchies are deep, you may get too large SQL statements.
     * <p>
     * ATTENTION! In statements on "subprocess" table, list of ALL process IDs appears twice.
     */
    private static final int ROOT_PROCESS_IDS_PER_STEP = 100;

    /**
     * First run on huge database may take long time, so prevent concurrent runs just in case.
     */
    private AtomicBoolean busy = new AtomicBoolean(false);

    /**
     * If false, execute() does nothing.
     */
    private boolean permanentFailure = false;

    private Dialect dialect = null;
    private DbType dbType = null;

    /**
     * Contains four "?" params:
     * <ol>
     *     <li>last process ID processed, so next step continues where previous step stopped (MS SQL does not support OFFSET+LIMIT, only LIMIT);
     *     <li>Current time.</li>
     *     <li>Current time again.</li>
     *     <li>LIMIT value.</li>
     * </ol>
     */
    private String sqlSelectRootProcessIds = null;

    /**
     * Contains substring "in ({rootIds})", where "{rootIds}" is a macro to be replaced by CSV like "1,2,3".
     */
    private String sqlSelectSubProcessIds = null;

    /**
     * Filled once per execute(), not per every step().
     */
    private Timestamp currentTime;

    private long lastHandledProcessId;
    private long totalProcessIdsHandled;

    public void execute() throws Exception {
        if (!ApplicationContextFactory.getInitializerLogic().isInitialized()) {
            // Do not interfere with migrations.
            return;
        }

        if (!SystemProperties.isProcessArchivingEnabled() || permanentFailure || !busy.compareAndSet(false, true)) {
            return;
        }
        try {
            log.info("Started");
            initializeOnce();
            currentTime = new Timestamp(System.currentTimeMillis());
            lastHandledProcessId = 0;
            totalProcessIdsHandled = 0;
            // Need this to call proxied @Transactional step() method:
            val myself = ApplicationContextFactory.getContext().getBean(ProcessArchiver.class);
            //noinspection StatementWithEmptyBody
            while (myself.step());
            log.info("Finished; archived " + totalProcessIdsHandled + " processes");
        } finally {
            busy.set(false);
        }
    }

    private void initializeOnce() {
        if (dialect != null && dbType != null && sqlSelectRootProcessIds != null && sqlSelectSubProcessIds != null) {
            return;
        }

        dialect = ApplicationContextFactory.getDialect();
        if (!dialect.supportsLimit()) {
            permanentFailure = true;
            throw new RuntimeException("Current database dialect " + dialect + " does not support LIMIT; ProcessArchiver disabled");
        }

        // There is no date / time / timestamp arithmetic in QueryDSL, neither in HQL / JPA. Must fallback to SQL.
        // And this arithmetic is different for different SQL servers.
        dbType = ApplicationContextFactory.getDbType();

        int defaultSecondsBeforeArchiving = SystemProperties.getProcessDefaultSecondsBeforeArchiving();

        // Since we don't have true tree closure with (root_id, root_id, 0) record,
        // we must check archiving condition separately for root processes and their subprocesses.
        // "Order by" is for determinism and to simplify updating lastHandledProcessId.
        sqlSelectRootProcessIds = dialect.getLimitString("select p.id " +
                "from bpm_process p " +
                "inner join bpm_process_definition_ver dv on (dv.id = p.definition_version_id) " +
                "inner join bpm_process_definition d on (d.id = dv.definition_id) " +
                // Continue since last step:
                "where p.id > ? and " +
                // Get only root process IDs:
                "      not exists (select s.process_id from bpm_subprocess s where s.process_id = p.id) and " +
                // Check condition for root processes:
                "      p.execution_status = 'ENDED' and " +
                "      " + generateEndDateCheckExpression("d", "p.end_date", defaultSecondsBeforeArchiving) + " and " +
                "      not exists (select t.process_id from bpm_task t where t.process_id = p.id) and " +
                "      not exists (select j.process_id from bpm_job j where j.process_id = p.id) and " +
                // Check no descendant processes exist that violate condition:
                "      not exists (" +
                "          select p2.id " +
                "          from bpm_subprocess s2 " +
                "          inner join bpm_process p2 on (p2.id = s2.process_id) " +
                "          inner join bpm_process_definition_ver dv2 on (dv2.id = p2.definition_version_id) " +
                "          inner join bpm_process_definition d2 on (d2.id = dv2.definition_id) " +
                "          where s2.root_process_id = p.id and (" +
                "                p2.execution_status <> 'ENDED' or " +
                "                not(" + generateEndDateCheckExpression("d2", "p2.end_date", defaultSecondsBeforeArchiving) + ") or " +
                "                exists (select t.process_id from bpm_task t where t.process_id = p2.id) or " +
                "                exists (select j.process_id from bpm_job j where j.process_id = p2.id) " +
                "          ) " +
                "      ) " +
                "order by p.id", 0, ROOT_PROCESS_IDS_PER_STEP);

        // "Order by" is for determinism and to simplify updating lastHandledProcessId.
        sqlSelectSubProcessIds = "select distinct process_id " +
                "from bpm_subprocess " +
                "where root_process_id in ({rootIds}) " +
                "order by process_id";
    }

    /**
     * Returned expression contains single "?" parameter for current time.
     * I could use NOW(), but Java and SQL timezones may differ, and END_DATE values are set by Java code when entities are stored.
     *
     * @param definitionAlias E.g. "d".
     * @param endDateField E.g. "p.end_date" for process, or "t.end_date" for token.
     */
    private String generateEndDateCheckExpression(String definitionAlias, String endDateField, int defaultSecondsBeforeArchiving) {
        // COALESCE function is the same in all supported SQL servers.
        val seconds = "coalesce(" + definitionAlias + ".seconds_before_archiving, " + defaultSecondsBeforeArchiving + ")";

        switch (dbType) {
            case H2:
            case HSQL:
                return "dateadd('second', " + seconds + ", " + endDateField + ") < ?";  // Works for H2.
            case MSSQL:
                return "dateadd(second, " + seconds + ", " + endDateField + ") < ?";
            case ORACLE:
                return "(" + endDateField + " + numtodsinterval(" + seconds + ", 'second')) < ?";
            case POSTGRESQL:
                return "(" + endDateField + " + cast(" + seconds + " || ' second' as interval)) < ?";
            default:
                permanentFailure = true;
                throw new RuntimeException("Unsupported dbType = " + dbType);
        }
    }

    /**
     * First run on huge database may take a long time, so instead of single huge transaction, we'll go in smaller transactional steps.
     *
     * @return False if complete.
     */
    @Transactional
    public boolean step() throws Exception {
        // With Hibernate 4+, use session.doReturningWork():
        val conn = ApplicationContextFactory.getSessionFactory().getCurrentSession().connection();

        val processIds = new ArrayList<Long>();

        try (val q = conn.prepareStatement(sqlSelectRootProcessIds)) {
            q.setLong(1, lastHandledProcessId);
            q.setTimestamp(2, currentTime);
            q.setTimestamp(3, currentTime);
            if (dialect.supportsVariableLimit()) {
                q.setInt(4, ROOT_PROCESS_IDS_PER_STEP);
            }
            val rs = q.executeQuery();
            while (rs.next()) {
                processIds.add(rs.getLong(1));
            }
        }
        if (processIds.isEmpty()) {
            log.debug("step(): processIds.size() = 0; done");
            return false;
        }
        // Do it twice, after both queries (both queries contain "order by id").
        lastHandledProcessId = Math.max(lastHandledProcessId, processIds.get(processIds.size() - 1).longValue());

        try (val q = conn.createStatement()) {
            val rs = q.executeQuery(sqlSelectSubProcessIds.replace("{rootIds}", StringUtils.join(processIds, ",")));
            while (rs.next()) {
                processIds.add(rs.getLong(1));
            }
        }
        // Do it twice, after both queries (both queries contain "order by id").
        lastHandledProcessId = Math.max(lastHandledProcessId, processIds.get(processIds.size() - 1).longValue());

        log.debug("step(): processIds.size() = " + processIds.size());
        totalProcessIdsHandled += processIds.size();

        try (val stmt = conn.createStatement()) {
            // ATTENTION! Don't Lists.partition(processIds), or you'll get FK violations if parent and child processes go into different partitions.
            val pidsCsv = "(" + StringUtils.join(processIds, ",") + ")";
            try {
                // Create rows in referenced tables first, then in referencing tables.

                // Refernces self, plus has root_token_id field.
                stmt.executeUpdate("insert into archived_process " +
                        "      (id, parent_id, tree_path, start_date, end_date, version, definition_version_id, root_token_id) " +
                        "select id, parent_id, tree_path, start_date, end_date, version, definition_version_id, root_token_id " +
                        "from bpm_process " +
                        "where id in " + pidsCsv
                );

                // References process and self.
                stmt.executeUpdate("insert into archived_token " +
                        "      (id, process_id, parent_id, error_message, transition_id, message_selector, start_date, end_date, error_date, node_id, reactivate_parent, node_type, version, name) " +
                        "select id, process_id, parent_id, error_message, transition_id, message_selector, start_date, end_date, error_date, node_id, reactivate_parent, node_type, version, name " +
                        "from bpm_token " +
                        "where process_id in " + pidsCsv
                );

                // References process, also has parent_token_id field.
                stmt.executeUpdate("insert into archived_subprocess " +
                        "      (id, process_id, parent_process_id, root_process_id, parent_node_id, create_date, subprocess_index, parent_token_id) " +
                        "select id, process_id, parent_process_id, root_process_id, parent_node_id, create_date, subprocess_index, parent_token_id " +
                        "from bpm_subprocess " +
                        "where process_id in " + pidsCsv + " or parent_process_id in " + pidsCsv
                );

                // References process.
                stmt.executeUpdate("insert into archived_swimlane " +
                        "      (id, process_id, create_date, name, version, executor_id) " +
                        "select id, process_id, create_date, name, version, executor_id " +
                        "from bpm_swimlane " +
                        "where process_id in " + pidsCsv
                );

                // References process.
                stmt.executeUpdate("insert into archived_variable " +
                        "      (discriminator, id, process_id, create_date, name, version, converter, bytes, stringvalue, longvalue, doublevalue, datevalue) " +
                        "select discriminator, id, process_id, create_date, name, version, converter, bytes, stringvalue, longvalue, doublevalue, datevalue " +
                        "from bpm_variable " +
                        "where process_id in " + pidsCsv
                );

                // No FKs, but has process_id and token_id fields.
                stmt.executeUpdate("insert into archived_log " +
                        "      (discriminator, id, process_id, node_id, token_id, create_date, severity, bytes, content) " +
                        "select discriminator, id, process_id, node_id, token_id, create_date, severity, bytes, content " +
                        "from bpm_log " +
                        "where process_id in " + pidsCsv
                );

                // No FKs, but has process_id field.
                stmt.executeUpdate("insert into archived_agglog_task " +
                        "      (id, initial_actor_name, complete_actor_name, end_reason, swimlane_name, token_id, task_name, task_id, create_date, end_date, deadline_date, node_id, task_index, process_id) " +
                        "select id, initial_actor_name, complete_actor_name, end_reason, swimlane_name, token_id, task_name, task_id, create_date, end_date, deadline_date, node_id, task_index, process_id " +
                        "from bpm_agglog_task " +
                        "where process_id in " + pidsCsv
                );

                // References archived_agglog_task.
                stmt.executeUpdate("insert into archived_agglog_assignment " +
                        "      (id, new_executor_name, old_executor_name, assignment_date, agglog_task_id) " +
                        "select id, new_executor_name, old_executor_name, assignment_date, agglog_task_id " +
                        "from bpm_agglog_assignment " +
                        "where agglog_task_id in (select id from bpm_agglog_task where process_id in " + pidsCsv + ")"
                );

                // Call handlers.
                for (String handlerClassName : SystemProperties.getProcessArchiverStepHandlers()) {
                    ProcessArchiverStepHandler handler = ClassLoaderUtil.instantiate(handlerClassName);
                    handler.handle(conn, processIds, pidsCsv);
                }

                // Delete rows in reverse order (from referencing tables first):

                // References archived_agglog_task.
                stmt.executeUpdate("delete from bpm_agglog_assignment where agglog_task_id in (select id from bpm_agglog_task where process_id in " + pidsCsv + ")");

                // No FKs, but has process_id field.
                stmt.executeUpdate("delete from bpm_agglog_task where process_id in " + pidsCsv);

                // No FKs, but has process_id and token_id fields.
                stmt.executeUpdate("delete from bpm_log where process_id in " + pidsCsv);

                // References process.
                stmt.executeUpdate("delete from bpm_variable where process_id in " + pidsCsv);

                // References process.
                stmt.executeUpdate("delete from bpm_swimlane where process_id in " + pidsCsv);

                // References process and token.
                stmt.executeUpdate("delete from bpm_subprocess where process_id in " + pidsCsv + " or parent_process_id in " + pidsCsv);

                // References token and self.
                // Since token references process, must null that references before deleting processes.
                // Also, I delete processes before tokens, because reverse FK cannot bpm_process.root_token_id is not null.
                stmt.executeUpdate("update bpm_process set parent_id = null where id in " + pidsCsv);
                stmt.executeUpdate("update bpm_token set process_id = null where process_id in " + pidsCsv);
                stmt.executeUpdate("delete from bpm_process where id in " + pidsCsv);

                // References process (already deleted above) and self.
                // Process_id is already nulled above, so cannot check it against pidsCsv, only against nulls.
                stmt.executeUpdate("update bpm_token set parent_id = null where process_id is null");
                stmt.executeUpdate("delete from bpm_token where process_id is null");
            } catch (Throwable e) {
                throw new RuntimeException("Failed for pidsCsv = " + pidsCsv, e);
            }
        }

        HibernateUtil.clearSecondLevelCaches(Process.class, NodeProcess.class, Token.class, Swimlane.class, ProcessLog.class, Variable.class);
        return true;
    }
}
