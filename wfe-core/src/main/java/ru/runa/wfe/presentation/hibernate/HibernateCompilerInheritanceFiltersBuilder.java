package ru.runa.wfe.presentation.hibernate;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.DbSource;
import ru.runa.wfe.presentation.DbSource.AccessType;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.presentation.FieldState;
import ru.runa.wfe.presentation.filter.FilterCriteria;
import ru.runa.wfe.presentation.filter.FilterCriteriaFactory;

/**
 * Builds filter SQL statements for fields with inheritance.
 */
@CommonsLog
public class HibernateCompilerInheritanceFiltersBuilder {

    /**
     * {@link BatchPresentation}, used to build query.
     */
    private final BatchPresentation batchPresentation;

    /**
     * Component to build HQL query for {@link BatchPresentation}.
     */
    private final HibernateCompilerHqlBuider hqlBuilder;

    /**
     * Translator, used to translate HQL query to SQL.
     */
    private final HibernateCompilerTranslator queryTranslator;

    /**
     * Constructs instance for building filter SQL statements for fields with inheritance.
     * 
     * @param batchPresentation
     *            {@link BatchPresentation}, used to build query.
     * @param hqlBuilder
     *            Component to build HQL query for {@link BatchPresentation}.
     * @param queryTranslator
     *            Translator, used to translate HQL query to SQL.
     */
    public HibernateCompilerInheritanceFiltersBuilder(BatchPresentation batchPresentation, HibernateCompilerHqlBuider hqlBuilder,
            HibernateCompilerTranslator queryTranslator) {
        this.batchPresentation = batchPresentation;
        this.hqlBuilder = hqlBuilder;
        this.queryTranslator = queryTranslator;
    }

    /**
     * Injects SQL statements to filter by fields with inheritance into SQL query.
     * 
     * @param sqlQuery
     *            SQL query to inject filter statements.
     */
    public void injectFiltersStatements(StringBuilder sqlQuery) {
        if (!hqlBuilder.isFilterByInheritance()) {
            return;
        }
        List<String> filters = buildFiltersStatements();
        int insertIdx = sqlQuery.indexOf(" where ");
        if (insertIdx > 0) {
            insertIdx = sqlQuery.indexOf(" order ");
            if (insertIdx == -1) {
                insertIdx = sqlQuery.length();
            }
        } else {
            insertIdx = sqlQuery.indexOf(" order by ");
            if (insertIdx == -1) {
                insertIdx = sqlQuery.length();
            }
            sqlQuery.insert(insertIdx, " where (1=1) ");
            insertIdx += 13;
        }
        for (String filter : filters) {
            sqlQuery.insert(insertIdx, ")").insert(insertIdx, filter).insert(insertIdx, " and (");
        }
    }

    /**
     * Creates SQL statements to filter by fields with inheritance.
     * 
     * @return List of SQL statements for fields with inheritance.
     */
    private List<String> buildFiltersStatements() {
        Map<Integer, FilterCriteria> filteredMap = batchPresentation.getFilteredFields();
        if (filteredMap.size() == 0) {
            return new ArrayList<>();
        }
        FieldDescriptor[] fields = batchPresentation.getAllFields();
        List<String> result = new LinkedList<>();
        for (Map.Entry<Integer, FilterCriteria> entry : filteredMap.entrySet()) {
            int index = entry.getKey();
            if (fields[index].filterMode != FieldFilterMode.DATABASE || fields[index].fieldState == FieldState.DISABLED) {
                continue;
            }
            if (fields[index].dbSources.length == 1) {
                continue; // Must be processed before (see HibernateCompilerHQLBuilder).
            }
            List<String> filters = buildFiltersForField(fields[index], entry.getValue());
            if (filters.isEmpty()) {
                continue;
            }
            StringBuilder filtersStatement = new StringBuilder("((1<>1)");
            for (String filter : filters) {
                filtersStatement.append(" OR (").append(filter).append(")");
            }
            filtersStatement.append(")");
            result.add(filtersStatement.toString());
        }
        return result;
    }

    /**
     * Creates SQL statements to filter by field. This statements must be joined with OR operation.
     * 
     * @param field
     *            Field, to create SQL statements.
     * @param criteria
     *            Filter criteria for field.
     * @return SQL statements to filter by field.
     */
    private List<String> buildFiltersForField(FieldDescriptor field, FilterCriteria criteria) {
        List<String> result = new LinkedList<>();
        for (DbSource dbSource : field.dbSources) {
            if (dbSource.getValueDBPath(AccessType.FILTER, null) == null) {
                continue;
            }
            String condition = createDbSourceFilterCriteria(field, dbSource, criteria.getFilterTemplates());
            if (condition == null) {
                continue;
            }
            String alias = hqlBuilder.getAliasMapping().getAlias(field);
            String fakeHQLRequest = "select " + alias + " from " + dbSource.getSourceObject().getName() + " as " + alias + " where " + condition;
            HibernateCompilerTranslator fakeTranslator = new HibernateCompilerTranslator(fakeHQLRequest, false);
            String fakeSQLRequest = fakeTranslator.translate();
            int filterExprIdx = fakeSQLRequest.indexOf(" where ") + 7;
            fakeSQLRequest = fakeSQLRequest.substring(filterExprIdx).replaceAll(fakeTranslator.getAliasName(alias),
                    queryTranslator.getAliasName(alias));
            StringBuilder sqlFilter = new StringBuilder(fakeSQLRequest);
            List<String> phSeq = HibernateCompilerPlaceholdersHelper.getPlaceholdersFromHQL(fakeHQLRequest, hqlBuilder.getPlaceholders());
            HibernateCompilerPlaceholdersHelper.restorePlaceholdersInSQL(sqlFilter, phSeq);
            result.add(sqlFilter.toString());
        }
        return result;
    }

    /**
     * Creates HQL condition string to filter by database source.
     * 
     * @param field
     *            Field, to create filter statements.
     * @param dbSource
     *            Components to access field value by HQL statement.
     * @param filterTemplates
     *            Filter templates.
     * @return HQL condition string to filter by database source.
     */
    private String createDbSourceFilterCriteria(FieldDescriptor field, DbSource dbSource, String[] filterTemplates) {
        FilterCriteria fieldsToFilterCriteria = FilterCriteriaFactory.createFilterCriteria(dbSource.getSourceObject());
        try {
            fieldsToFilterCriteria.applyFilterTemplates(filterTemplates);
            return fieldsToFilterCriteria.buildWhereCondition(
                    dbSource.getValueDBPath(AccessType.FILTER, hqlBuilder.getAliasMapping().getAlias(field)), hqlBuilder.getPlaceholders());
        } catch (Exception e) {
            log.error("Filter can't be applied to field with inheritance. Field name is " + field.displayName + ", database source is "
                    + dbSource.getSourceObject().getName(), e);
            return null;
        }
    }
}
