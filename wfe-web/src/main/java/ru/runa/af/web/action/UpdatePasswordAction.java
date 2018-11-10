package ru.runa.af.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.af.web.form.UpdatePasswordForm;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdForm;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;

/**
 * Created on 24.08.2004
 * 
 * @struts:action path="/updatePassword" name="updatePasswordForm"
 *                validate="true" input = "/WEB-INF/af/manage_executor.jsp"
 * @struts.action-forward name="success" path="/manage_executor.do" redirect =
 *                        "true"
 * @struts.action-forward name="failure" path="/manage_executor.do" redirect =
 *                        "true"
 * @struts.action-forward name="failure_executor_does_not_exist"
 *                        path="/manage_executors.do" redirect = "true"
 */
public class UpdatePasswordAction extends ActionBase {

    public static final String ACTION_PATH = "/updatePassword";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        UpdatePasswordForm form = (UpdatePasswordForm) actionForm;
        try {
            ExecutorService executorService = Delegates.getExecutorService();
            Actor actor = executorService.getExecutor(getLoggedUser(request), form.getId());
            executorService.setPassword(getLoggedUser(request), actor, form.getPassword());
        } catch (Exception e) {
            addError(request, e);
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), IdForm.ID_INPUT_NAME, form.getId());
        }
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), IdForm.ID_INPUT_NAME, form.getId());
    }

}
