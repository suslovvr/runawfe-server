package ru.runa.wfe.web.api;

import javax.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.val;
import ru.runa.common.web.Commons;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.service.delegate.Delegates;

public class GetProcessDefs extends Api<Api.EmptyRequest, Api.ListResponse<GetProcessDefs.Row>> {

    @Getter
    static class Row {
        long id;
        String name;
        String description;
        String category;
    }

    @Override
    protected ListResponse<Row> execute(EmptyRequest form, HttpServletRequest hrq) {
        val user = Commons.getUser(hrq.getSession());
        val oo = Delegates.getDefinitionService().getProcessDefinitions(user, null, false);
        return new ListResponse<Row>(oo.size()) {{
            for (WfDefinition o : oo) {
                getRows().add(new Row() {{
                    id = o.getId();
                    name = o.getName();
                    description = o.getDescription();

                    val cc = o.getCategories();
                    if (cc.length > 0) {
                        category = cc[cc.length - 1];
                    }
                    if (category == null) {
                        category = "";
                    }
                }});
            }
        }};
    }
}
