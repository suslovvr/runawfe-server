package ru.runa.wf.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.form.IdVersionForm;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wfe.service.delegate.Delegates;

public class UpgradeProcessToDefinitionVersionAction extends ActionBase {
    public static final String ACTION_PATH = "/upgradeProcessToDefinitionVersion";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse responce) {
        IdVersionForm form = (IdVersionForm) actionForm;
        try {
            if (Delegates.getExecutionService().upgradeProcessToDefinitionVersion(getLoggedUser(request), form.getId(), form.getVersion())) {
                addMessage(request, new ActionMessage(MessagesProcesses.PROCESS_UPGRADED_TO_DEFINITION_VERSION.getKey()));
            }
            return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), IdForm.ID_INPUT_NAME, form.getId());
        } catch (Exception e) {
            addError(request, e);
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), IdForm.ID_INPUT_NAME, form.getId());
        }
    }

}
