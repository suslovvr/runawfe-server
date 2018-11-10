package ru.runa.wf.web.html;

import java.util.Date;

import org.apache.ecs.html.TD;

import ru.runa.common.web.html.TdBuilder;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.task.dto.WfTask;

/**
 * Class for displaying task creation date (Task.createDate) in the task list table
 * 
 * @author Vladimir Shevtsov
 *
 */
public final class TaskCreationDateTdBuilder implements TdBuilder {

    @Override
    public TD build(Object object, Env env) {
        TD td = new TD();
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        td.addElement(getValue(object, env));
        return td;
    }

    @Override
    public String getValue(Object object, Env env) {
        Date creationDate = ((WfTask) object).getCreationDate();
        if (creationDate == null) {
            return "";
        }
        return CalendarUtil.formatDateTime(creationDate);
    }

    @Override
    public String[] getSeparatedValues(Object object, Env env) {
        return new String[] { getValue(object, env) };
    }

    @Override
    public int getSeparatedValuesCount(Object object, Env env) {
        return 1;
    }

}
