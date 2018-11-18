package ru.runa.wfe.web.api;

import javax.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.val;
import ru.runa.common.web.Commons;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;

public class GetMyTasks extends Api<Api.EmptyRequest, Api.ListResponse<GetMyTasks.Row>> {

    @Getter
    static class Row {
        long id;
        String name;
        String description;
        String definitionName;
        String creationDate;
        String deadlineDate;
    }

    @Override
    protected ListResponse<Row> execute(EmptyRequest form, HttpServletRequest hrq) {
        val user = Commons.getUser(hrq.getSession());
        val oo = Delegates.getTaskService().getMyTasks(user, null);
        return new Api.ListResponse<Row>(oo.size()) {{
            for (WfTask o : oo) {
                getRows().add(new Row() {{
                    id = o.getId();
                    name = o.getName();
                    description = o.getDescription();
                    definitionName = o.getDefinitionName();
                    creationDate = CalendarUtil.formatDate(o.getCreationDate());
                    deadlineDate = CalendarUtil.formatDate(o.getDeadlineDate());
                }});
            }
        }};
    }
}
