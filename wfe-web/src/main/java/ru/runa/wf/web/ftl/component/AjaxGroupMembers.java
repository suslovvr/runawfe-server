package ru.runa.wf.web.ftl.component;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.ftl.AjaxJsonFormComponent;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.client.DelegateExecutorLoader;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Group;

@SuppressWarnings("unchecked")
public class AjaxGroupMembers extends AjaxJsonFormComponent {
    private static final long serialVersionUID = 1L;

    @Override
    protected String renderRequest() {
        String groupVariableName = getParameterAsString(0);
        String groupScriptingVariableName = variableProvider.getVariableNotNull(groupVariableName).getDefinition().getScriptingNameWithoutDots();
        String userVariableName = getParameterAsString(1);
        String userScriptingVariableName = variableProvider.getVariableNotNull(userVariableName).getDefinition().getScriptingNameWithoutDots();
        Map<String, String> substitutions = Maps.newHashMap();
        substitutions.put("groupSelectorId", groupScriptingVariableName);
        substitutions.put("userSelectorId", userScriptingVariableName);
        StringBuffer html = new StringBuffer();
        html.append(exportScript(substitutions, true));
        html.append("<div class=\"ajaxGroupMembers\">");
        html.append("<div id=\"ajaxGroupMembers_").append(groupScriptingVariableName).append("\">");
        html.append("<select id=\"").append(groupScriptingVariableName).append("\" name=\"").append(groupVariableName);
        html.append("\" style=\"width: auto;\">");
        List<Group> groups = (List<Group>) Delegates.getExecutorService().getExecutors(user, BatchPresentationFactory.GROUPS.createNonPaged());
        Group defaultGroup = variableProvider.getValue(Group.class, groupVariableName);
        html.append("<option value=\"\"> ------------------------- </option>");
        for (Group group : groups) {
            html.append("<option value=\"ID").append(group.getId()).append("\"");
            if (Objects.equal(defaultGroup, group)) {
                html.append(" selected");
            }
            html.append(">").append(group.getName()).append("</option>");
        }
        html.append("</select></div>");
        html.append("<div id=\"ajaxGroupMembers_").append(userScriptingVariableName).append("\">");
        html.append("<select id=\"").append(userScriptingVariableName).append("\" name=\"").append(userVariableName).append("\">");
        html.append("<option value=\"\"> ------------------------- </option>");
        if (defaultGroup != null) {
            List<Actor> actors = Delegates.getExecutorService().getGroupActors(user, defaultGroup);
            Actor defaultActor = variableProvider.getValue(Actor.class, userVariableName);
            for (Actor actor : actors) {
                html.append("<option value=\"ID").append(actor.getId()).append("\"");
                if (Objects.equal(defaultActor, actor)) {
                    html.append(" selected");
                }
                html.append(">").append(actor.getFullName()).append("</option>");
            }
        }
        html.append("</select></div>");
        html.append("</div>");
        return html.toString();
    }

    @Override
    protected JSONAware processAjaxRequest(HttpServletRequest request) throws Exception {
        JSONArray json = new JSONArray();
        Group group = (Group) TypeConversionUtil.convertToExecutor(request.getParameter("groupId"), new DelegateExecutorLoader(user));
        List<Actor> actors = Delegates.getExecutorService().getGroupActors(user, group);
        json.add(createJsonObject(null, " ------------------------- "));
        for (Actor actor : actors) {
            json.add(createJsonObject(actor.getId(), actor.getFullName()));
        }
        return json;
    }

    private JSONObject createJsonObject(Long id, String name) {
        JSONObject object = new JSONObject();
        object.put("id", id != null ? "ID" + id : "");
        object.put("name", name);
        return object;
    }

}
