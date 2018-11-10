package ru.runa.wfe.presentation.filter;

import com.google.common.base.Strings;
import java.util.Date;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.presentation.hibernate.QueryParametersMap;

@CommonsLog
public class DateFilterCriteria extends FilterCriteria {
    private static final long serialVersionUID = 1L;

    private Date dateStart;
    private Date dateEnd;

    public DateFilterCriteria() {
        super(2);
    }

    public DateFilterCriteria(Date fromDate, Date toDate) {
        applyFilterTemplates(new String[] { CalendarUtil.formatDateTime(fromDate), CalendarUtil.formatDateTime(toDate) });
    }

    @Override
    protected void validate(String[] newTemplates) throws FilterFormatException {
        super.validate(newTemplates);
        try {
            if (!Strings.isNullOrEmpty(newTemplates[0])) {
                CalendarUtil.convertToDate(newTemplates[0], CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT);
            }
            if (!Strings.isNullOrEmpty(newTemplates[1])) {
                CalendarUtil.convertToDate(newTemplates[1], CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT);
            }
        } catch (Exception e) {
            throw new FilterFormatException(e.getMessage());
        }
    }

    private void initDates() {
        try {
            if (!Strings.isNullOrEmpty(getFilterTemplate(0))) {
                dateStart = CalendarUtil.convertToDate(getFilterTemplate(0), CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT);
            }
            if (!Strings.isNullOrEmpty(getFilterTemplate(1))) {
                dateEnd = CalendarUtil.convertToDate(getFilterTemplate(1), CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT);
            }
        } catch (Exception e) {
            log.error("date parsing error: " + e);
        }
    }

    @Override
    public String buildWhereCondition(String aliasedFieldName, QueryParametersMap placeholders) {
        initDates();

        String placeholderStart = makePlaceHolderName(aliasedFieldName + "Start");
        String placeholderEnd = makePlaceHolderName(aliasedFieldName + "End");

        StringBuilder whereStringBuilder = new StringBuilder(aliasedFieldName);

        if (dateStart == null) {
            if (dateEnd == null) {
                // empty date (NULL value)
                whereStringBuilder.append(" is null");
            } else {
                // less than
                whereStringBuilder.append(" < :").append(placeholderEnd);
            }
        } else {
            if (dateEnd == null) {
                // more than
                whereStringBuilder.append(" > :").append(placeholderStart);
            } else {
                // between
                whereStringBuilder.append(" between :");
                whereStringBuilder.append(placeholderStart);
                whereStringBuilder.append(" and :");
                whereStringBuilder.append(placeholderEnd);
            }
        }

        if (dateStart != null) {
            placeholders.add(placeholderStart, dateStart);
        }
        if (dateEnd != null) {
            placeholders.add(placeholderEnd, dateEnd);
        }

        whereStringBuilder.append(" ");
        return whereStringBuilder.toString();
    }
}
