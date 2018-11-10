package ru.runa.wf.web.html;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.servlet.jsp.PageContext;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Resources;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.wf.web.tag.ListTasksFormTag;
import ru.runa.wfe.audit.TaskAssignLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.TaskDeadlineUtils;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;

public class ProcessSwimlaneAssignmentRowBuilder implements RowBuilder {
    private final User user;
    private final Iterator<WfTask> iterator;
    private final PageContext pageContext;

    public ProcessSwimlaneAssignmentRowBuilder(User user, List<WfTask> activeTasks, PageContext pageContext) {
        this.user = user;
        this.pageContext = pageContext;
        iterator = activeTasks.iterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public TR buildNext() {
        WfTask task = iterator.next();
        TR tr = new TR();

        ListTasksFormTag.TasksCssClassStrategy cssClassStrategy = new ListTasksFormTag.TasksCssClassStrategy();
        String cssClass = cssClassStrategy.getClassName(task, user);

        tr.setClass(cssClass);
        tr.addElement(new TD(task.getName()).setClass(Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TD(task.getSwimlaneName()).setClass(Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TD(HTMLUtils.createExecutorElement(user, pageContext, task.getOwner())).setClass(Resources.CLASS_LIST_TABLE_TD));

        tr.addElement(new TD(CalendarUtil.formatDateTime(task.getCreationDate())).setClass(Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TaskDeadlineTdBuilder().build(task, null).setClass(Resources.CLASS_LIST_TABLE_TD));

        Date currentDate = new Date();
        String duration = TaskDeadlineUtils.calculateTimeDuration(task.getCreationDate(), currentDate);
        tr.addElement(new TD().addElement(duration).setClass(Resources.CLASS_LIST_TABLE_TD));

        String deadLineDuration = TaskDeadlineUtils.calculateTimeDuration(currentDate, task.getDeadlineDate());
        tr.addElement(new TD().addElement(deadLineDuration).setClass(Resources.CLASS_LIST_TABLE_TD));

        String startExecutionDateString = "";
        ProcessLogFilter filter = new ProcessLogFilter(task.getProcessId());
        filter.setNodeId(task.getNodeId());
        ProcessLogs logs = Delegates.getAuditService().getProcessLogs(user, filter);

        TaskAssignLog taskAssignLog = logs.getLastOrNull(TaskAssignLog.class);
        if (taskAssignLog != null) {
            startExecutionDateString = CalendarUtil.formatDateTime(taskAssignLog.getCreateDate());
        }

        tr.addElement(new TD(startExecutionDateString).setClass(Resources.CLASS_LIST_TABLE_TD));

        return tr;
    }

    @Override
    public List<TR> buildNextArray() {
        return null;
    }
}
