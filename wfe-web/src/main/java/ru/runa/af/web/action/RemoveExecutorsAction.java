package ru.runa.af.web.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdsForm;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.collect.Lists;

/**
 * Created on 18.08.2004
 * 
 * @struts:action path="/removeExecutors" name="idsForm" validate="false"
 * @struts.action-forward name="success" path="/manage_executors.do" redirect =
 *                        "true"
 * @struts.action-forward name="failure" path="/manage_executors.do" redirect =
 *                        "true"
 */
public class RemoveExecutorsAction extends ActionBase {

    public static final String ACTION_PATH = "/removeExecutors";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse responce) {
        try {
            List<Long> ids = Lists.newArrayList(((IdsForm) form).getIds());
            Delegates.getExecutorService().remove(getLoggedUser(request), ids);
        } catch (Exception e) {
            addError(request, e);
            return mapping.findForward(ru.runa.common.web.Resources.FORWARD_FAILURE);
        }
        return mapping.findForward(ru.runa.common.web.Resources.FORWARD_SUCCESS);
    }

}
