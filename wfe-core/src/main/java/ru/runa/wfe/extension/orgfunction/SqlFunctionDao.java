package ru.runa.wfe.extension.orgfunction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.SqlCommons;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

/**
 * Created on 10.05.2005
 * 
 */
public class SqlFunctionDao {
    private static List<Long> directorsCodesList;

    /**
     * Returns codes of actors selected by sql query. e.g. chief query - select
     * BOSS_ID from EMPLOYEES where ID = ? e.g. direct subordinate query -
     * select ID from EMPLOYEES where "BOSS_ID" = ?
     * 
     * @param sql
     *            sql query
     * @param parameters
     *            parameters of query
     * @return codes of actors
     */
    public static List<Long> getActorCodes(String sql, Object[] parameters) {
        Preconditions.checkNotNull(parameters);
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = getConnection();
            ps = con.prepareStatement(sql);
            setParameters(ps, parameters);
            ResultSet rs = ps.executeQuery();
            return getCodesFromResultSet(rs);
        } catch (SQLException e) {
            throw Throwables.propagate(e);
        } finally {
            SqlCommons.releaseResources(con, ps);
        }
    }

    /**
     * Returns codes of actors selected by sql query recursevly. e.g. indirect
     * subordinate org function query - select ID from EMPLOYEES where "BOSS_ID"
     * = ? will return all subordinates of employee with given ID and
     * subordinates on those subordinates and so on.
     * 
     * @param sql
     *            sql query
     * @param parameters
     *            parameters of query
     * @return codes of actors
     */
    public static List<Long> getActorCodesRecurisve(String sql, Object[] parameters) {
        Preconditions.checkNotNull(parameters);
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = getConnection();
            ps = con.prepareStatement(sql);
            List<Long> codeSet = Lists.newArrayList();
            getCodesRecursive(ps, codeSet, parameters);
            return codeSet;
        } catch (SQLException e) {
            throw Throwables.propagate(e);
        } finally {
            SqlCommons.releaseResources(con, ps);
        }
    }

    private static void getCodesRecursive(PreparedStatement ps, List<Long> codeSet, Object[] parameters) throws SQLException {
        setParameters(ps, parameters);
        ResultSet rs = ps.executeQuery();
        List<Long> codeList = getCodesFromResultSet(rs);
        rs.close();
        for (int i = 0; i < codeList.size(); i++) {
            Long code = codeList.get(i);
            if (!codeSet.contains(code)) {
                if (!codeSet.add(code)) {
                    // i.e. we have circle in hierarchy
                    throw new InternalApplicationException("Code hierarchy contains cycle");
                }
                parameters[0] = code;
                getCodesRecursive(ps, codeSet, parameters);
            }
        }
    }

    private static void initializeDirectorCodesList(String sql) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = getConnection();
            ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            directorsCodesList = getCodesFromResultSet(rs);
        } catch (SQLException e) {
            throw Throwables.propagate(e);
        } finally {
            SqlCommons.releaseResources(con, ps);
        }
    }

    public static List<Long> getDirectorCode(String sql, String chiefSQL, Long code) {
        List<Long> result = Lists.newArrayList();
        if (directorsCodesList == null) {
            initializeDirectorCodesList(sql);
        }
        if (directorsCodesList.contains(code)) {
            result.add(code);
            return result;
        }

        ArrayList<Long> codes = new ArrayList<Long>();
        codes.add(code);
        if (directorsCodesList.contains(code)) {
            result.add(code);
            return result;
        }
        while (!codes.isEmpty()) {
            List<Long> chiefsCodes = getActorCodes(SqlFunctionResources.getChiefCodeBySubordinateCodeSQL(), new Long[] { codes.get(0) });
            for (Long chiefCode : chiefsCodes) {
                if (directorsCodesList.contains(chiefCode)) {
                    result.add(chiefCode);
                    return result;
                }
                codes.add(chiefCode);
            }
            codes.remove(0);
        }
        throw new InternalApplicationException("Code hierarchy contains no director for actor with code = " + code);
    }

    private static List<Long> getCodesFromResultSet(ResultSet rs) throws SQLException {
        List<Long> codeList = Lists.newArrayList();
        while (rs.next()) {
            codeList.add(rs.getLong(1));
        }
        return codeList;
    }

    private static void setParameters(PreparedStatement ps, Object[] parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            ps.setObject(i + 1, parameters[i]);
        }
    }

    private static Context context;

    private static Context getInitialContext() {
        if (context == null) {
            try {
                context = new InitialContext();
            } catch (NamingException e) {
                throw Throwables.propagate(e);
            }
        }
        return context;
    }

    private static Connection getConnection() throws SQLException {
        try {
            DataSource ds = (DataSource) getInitialContext().lookup(SqlFunctionResources.getDataSourceName());
            if (ds == null) {
                throw new InternalApplicationException("No DataSource found for " + SqlFunctionResources.getDataSourceName());
            }
            return ds.getConnection();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

}
