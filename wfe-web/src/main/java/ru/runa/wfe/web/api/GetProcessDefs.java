package ru.runa.wfe.web.api;

import lombok.Getter;
import lombok.val;
import ru.runa.common.web.Commons;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.web.framework.extra.JsonHandler;

public class GetProcessDefs extends JsonHandler<Object, JsonHandler.ListResponse<GetProcessDefs.Row>> {

    @Getter
    static class Row {
        long id;
        String name;
        String description;
        String category;
    }

    public GetProcessDefs() {
        super(acceptGet, Object.class);
    }

    @Override
    protected ListResponse<Row> executeImpl() {
        val user = Commons.getUser(httpServletRequest.getSession());
        val oo = Delegates.getDefinitionService().getProcessDefinitions(user, null, false);
        return new ListResponse<Row>(user, oo.size()) {{
            for (WfDefinition o : oo) {
                val row = new Row();
                row.id = o.getId();
                row.name = o.getName();
                row.description = o.getDescription();

                val cc = o.getCategories();
                if (cc.length > 0) {
                    row.category = cc[cc.length - 1];
                }
                if (row.category == null) {
                    row.category = "";
                }

                getRows().add(row);
            }
        }};
    }
}
