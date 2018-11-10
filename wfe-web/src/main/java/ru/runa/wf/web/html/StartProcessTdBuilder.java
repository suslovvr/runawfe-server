package ru.runa.wf.web.html;

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.TD;
import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.BaseTdBuilder;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.StartDisabledImageProcessAction;
import ru.runa.wf.web.action.StartImageProcessAction;
import ru.runa.wf.web.tag.DefinitionUrlStrategy;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * @author Gordienko_m
 * @author Vitaliy S
 */
public class StartProcessTdBuilder extends BaseTdBuilder {

    public StartProcessTdBuilder() {
        super(Permission.START);
    }

    @Override
    public TD build(Object object, Env env) {
        WfDefinition definition = (WfDefinition) object;
        ConcreteElement startLink;

        String href;
        if (definition.isCanBeStarted()) {
            if (definition.hasStartImage()) {
                href = Commons.getActionUrl(StartImageProcessAction.ACTION_PATH, IdForm.ID_INPUT_NAME, definition.getVersionId(),
                        env.getPageContext(), PortletUrlType.Resource);
            } else {
                href = Commons.getUrl(WebResources.START_PROCESS_IMAGE, env.getPageContext(), PortletUrlType.Resource);
            }
        } else {
            if (definition.hasDisabledImage()) {
                href = Commons.getActionUrl(StartDisabledImageProcessAction.ACTION_PATH, IdForm.ID_INPUT_NAME, definition.getVersionId(),
                        env.getPageContext(), PortletUrlType.Resource);
            } else {
                href = Commons.getUrl(WebResources.START_PROCESS_DISABLED_IMAGE, env.getPageContext(), PortletUrlType.Resource);
            }
        }
        IMG startImg = new IMG(href);
        String startMessage = MessagesProcesses.LABEL_START_PROCESS.message(env.getPageContext());
        startImg.setAlt(startMessage);
        startImg.setBorder(0);
        if (definition.isCanBeStarted()) {
            String url = new DefinitionUrlStrategy(env.getPageContext()).getUrl(WebResources.ACTION_MAPPING_START_PROCESS, definition);
            startLink = new A(url).addElement(startImg);
            if (ConfirmationPopupHelper.getInstance().isEnabled(ConfirmationPopupHelper.START_PROCESS_PARAMETER)
                    || ConfirmationPopupHelper.getInstance().isEnabled(ConfirmationPopupHelper.START_PROCESS_FORM_PARAMETER)) {
                Interaction interaction = Delegates.getDefinitionService().getStartInteraction(env.getUser(), definition.getVersionId());
                if (!(interaction.hasForm() || interaction.getOutputTransitions().size() > 1)) {
                    String actionParameter = ConfirmationPopupHelper.START_PROCESS_FORM_PARAMETER;
                    startLink.addAttribute("onclick",
                            ConfirmationPopupHelper.getInstance().getConfirmationPopupCodeHTML(actionParameter, env.getPageContext()));
                }
            }
        } else {
            startLink = new StringElement().addElement(startImg);
        }
        TD td = new TD(startLink);
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        return td;
    }

    @Override
    public String getValue(Object object, Env env) {
        WfDefinition pd = (WfDefinition) object;
        return String.valueOf(pd.getVersionId());
    }
}
