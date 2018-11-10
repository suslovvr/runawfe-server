package ru.runa.wfe.commons;

import com.google.common.base.Preconditions;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.PostConstruct;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * For low-level JDBC logic.
 */
@Component
public class ManualTransactionManager {

    public static abstract class TxBase {
        protected final int executeUpdates(Connection conn, String... sqls) throws SQLException {
            try (Statement stmt = conn.createStatement()) {
                int result = 0;
                for (String sql : sqls) {
                    result = stmt.executeUpdate(sql);
                }
                return result;
            }
        }
    }

    public static abstract class TxCallable<R> extends TxBase {
        public abstract R call(Connection conn) throws Exception;
    }

    public static abstract class TxRunnable extends TxBase {
        public abstract void run(Connection conn) throws Exception;
    }


    @Autowired
    private PlatformTransactionManager txManager;
    @Autowired
    private SessionFactory sessionFactory;

    private DefaultTransactionDefinition txDefinition;


    @PostConstruct
    public void init() {
        Preconditions.checkNotNull(txManager);
        txDefinition = new DefaultTransactionDefinition();
        txDefinition.setName("WfeManual");
    }


    public <R> R callInTransaction(TxCallable<R> callable) throws Exception {
        R result;
        TransactionStatus txStatus = txManager.getTransaction(txDefinition);
        try {
            result = callable.call(sessionFactory.getCurrentSession().connection());
        } catch (Throwable e) {
            txManager.rollback(txStatus);
            throw e;
        }
        txManager.commit(txStatus);
        return result;
    }

    public void runInTransaction(TxRunnable runnable) throws Exception {
        callInTransaction(new TxCallable<Object>() {
            @Override
            public Object call(Connection conn) throws Exception {
                runnable.run(conn);
                return null;
            }
        });
    }
}
