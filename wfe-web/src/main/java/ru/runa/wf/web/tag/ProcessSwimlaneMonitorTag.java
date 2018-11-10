package ru.runa.wf.web.tag;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.MessagesExecutor;
import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.StringsHeaderBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.html.ProcessSwimlaneRowBuilder;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.execution.dto.WfSwimlane;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "processSwimlaneMonitor")
public class ProcessSwimlaneMonitorTag extends ProcessBaseFormTag {

    private static final long serialVersionUID = -5024428545159087986L;

    @Override
    protected boolean isSubmitButtonVisible() {
        return false;
    }

    @Override
    protected void fillFormData(TD tdFormElement) {
        if (WebResources.isUpdateProcessSwimlanesEnabled() && Delegates.getExecutorService().isAdministrator(getUser())) {
            Table table = new Table();
            tdFormElement.addElement(table);
            table.addAttribute("width", "100%");

            TR updateVariableTR = new TR();
            table.addElement(updateVariableTR);

            Map<String, Object> params = Maps.newHashMap();
            params.put("id", getIdentifiableId());
            String updateSwimlaneUrl = Commons.getActionUrl(WebResources.ACTION_UPDATE_PROCESS_SWIMLANES, params, pageContext, PortletUrlType.Render);
            A a = new A(updateSwimlaneUrl, MessagesProcesses.LINK_UPDATE_SWIMLANE.message(pageContext));
            updateVariableTR.addElement(new TD(a).addAttribute("align", "right"));
        }
        List<WfSwimlane> swimlanes = Delegates.getExecutionService().getProcessSwimlanes(getUser(), getIdentifiableId());
        List<String> headerNames = Lists.newArrayList();
        headerNames.add(MessagesProcesses.LABEL_SWIMLANE_NAME.message(pageContext));
        headerNames.add(MessagesProcesses.LABEL_GLOBAL.message(pageContext));
        headerNames.add(MessagesProcesses.LABEL_SWIMLANE_ASSIGNMENT.message(pageContext));
        headerNames.add(MessagesExecutor.LABEL_SWIMLANE_ORGFUNCTION.message(pageContext));
        HeaderBuilder headerBuilder = new StringsHeaderBuilder(headerNames);
        RowBuilder rowBuilder = new ProcessSwimlaneRowBuilder(swimlanes, pageContext);
        tdFormElement.addElement(new TableBuilder().build(headerBuilder, rowBuilder));
    }

    @Override
    protected Permission getSubmitPermission() {
        return Permission.LIST;
    }

    @Override
    protected String getTitle() {
        return MessagesProcesses.TITLE_INSANCE_SWINLANE_LIST.message(pageContext);
    }
}
