package ru.runa.af.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdsForm;
import ru.runa.wfe.service.delegate.Delegates;

public class RemoveRelationAction extends ActionBase {
    public static final String ACTION_PATH = "/removeRelation";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse responce) {
        try {
            IdsForm listAllForm = (IdsForm) form;
            for (Long id : listAllForm.getIds()) {
                Delegates.getRelationService().removeRelation(getLoggedUser(request), id);
            }
        } catch (Exception e) {
            addError(request, e);
            return mapping.findForward(ru.runa.common.web.Resources.FORWARD_FAILURE);
        }
        return mapping.findForward(ru.runa.common.web.Resources.FORWARD_SUCCESS);
    }
}
