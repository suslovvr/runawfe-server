package ru.runa.wfe.commons.web;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.SqlCommons;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.user.User;

import com.google.common.base.Strings;

public class SqlTreeViewCommand extends JsonAjaxCommand implements InitializingBean {
    private DataSource dataSource;
    private String tableName;
    private Class<?> idsClass = Long.class;
    private String parentIdColumnName;
    private String idColumnName;
    private String labelColumnName;
    private String selectableColumnName;
    private String sqlGetRoots;
    private String sqlGetChildren;
    private String sqlHasChildren;

    @Required
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Required
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setIdsClassName(String idsClassName) {
        this.idsClass = ClassLoaderUtil.loadClass(idsClassName);
    }

    @Required
    public void setParentIdColumnName(String parentIdColumnName) {
        this.parentIdColumnName = parentIdColumnName;
    }

    @Required
    public void setIdColumnName(String idColumnName) {
        this.idColumnName = idColumnName;
    }

    @Required
    public void setLabelColumnName(String labelColumnName) {
        this.labelColumnName = labelColumnName;
    }

    public void setSelectableColumnName(String selectableColumnName) {
        this.selectableColumnName = selectableColumnName;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        sqlGetRoots = "SELECT * FROM " + tableName + " WHERE " + parentIdColumnName + " IS NULL";
        sqlGetChildren = "SELECT * FROM " + tableName + " WHERE " + parentIdColumnName + "=?";
        sqlHasChildren = "SELECT COUNT(*) FROM " + tableName + " WHERE " + parentIdColumnName + "=?";
        log.debug("sqlGetRoots = " + sqlGetRoots);
        log.debug("sqlGetChildren = " + sqlGetChildren);
        log.debug("sqlHasChildren = " + sqlHasChildren);
    }

    @Override
    protected JSONAware execute(User user, HttpServletRequest request) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        PreparedStatement countStatement = null;
        try {
            connection = dataSource.getConnection();
            String parentIdString = request.getParameter("parentId");
            if (Strings.isNullOrEmpty(parentIdString)) {
                statement = connection.prepareStatement(sqlGetRoots);
            } else {
                statement = connection.prepareStatement(sqlGetChildren);
                Object parentId = TypeConversionUtil.convertTo(idsClass, parentIdString);
                statement.setObject(1, parentId);
            }
            countStatement = connection.prepareStatement(sqlHasChildren);
            resultSet = statement.executeQuery();
            JSONArray array = new JSONArray();
            while (resultSet.next()) {
                JSONObject object = new JSONObject();
                Object id = resultSet.getObject(idColumnName);
                object.put("id", id);
                object.put("label", resultSet.getString(labelColumnName));
                object.put("selectable", selectableColumnName != null ? resultSet.getBoolean(selectableColumnName) : true);
                ResultSet countResultSet = null;
                try {
                    countStatement.setObject(1, id);
                    countResultSet = countStatement.executeQuery();
                    countResultSet.next();
                    object.put("hasChildren", countResultSet.getInt(1) > 0);
                } finally {
                    SqlCommons.releaseResources(countResultSet);
                }
                array.add(object);
            }
            if (array.size() > 0) {
                ((JSONObject) array.get(array.size() - 1)).put("last", true);
            }
            return array;
        } finally {
            SqlCommons.releaseResources(countStatement);
            SqlCommons.releaseResources(connection, statement, resultSet);
        }
    }

}
