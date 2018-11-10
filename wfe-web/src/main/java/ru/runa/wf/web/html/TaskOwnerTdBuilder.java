package ru.runa.wf.web.html;

import org.apache.ecs.html.TD;

import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Executor;

/**
 * Created on 24.07.2007
 *
 * @author Konstantinov A.
 */
public class TaskOwnerTdBuilder implements TdBuilder {

    public TaskOwnerTdBuilder() {
    }

    private Executor getOwner(Object object) {
        WfTask task = (WfTask) object;
        if (task.isAcquiredBySubstitution()) {
            return task.getTargetActor();
        } else {
            return task.getOwner();
        }
    }

    @Override
    public TD build(Object object, Env env) {
        TD td = new TD(HTMLUtils.createExecutorElement(env.getPageContext(), getOwner(object)));
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        return td;
    }

    @Override
    public String getValue(Object object, Env env) {
        if (env.getPageContext() != null) {
            return HTMLUtils.getExecutorName(getOwner(object), env.getPageContext());
        }
        return getOwner(object).getLabel();
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
