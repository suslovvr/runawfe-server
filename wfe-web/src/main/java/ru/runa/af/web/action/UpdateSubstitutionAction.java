package ru.runa.af.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.af.web.form.SubstitutionForm;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.ss.TerminatorSubstitution;

/**
 * @struts:action path="/updateSubstitution" name="substitutionForm"
 *                validate="true" input = "/WEB-INF/af/edit_substitution.jsp"
 * @struts.action-forward name="success" path="/WEB-INF/af/manage_executor.jsp"
 * @struts.action-forward name="failure"
 *                        path="/WEB-INF/af/edit_substitution.jsp"
 */
public class UpdateSubstitutionAction extends ActionBase {
    public static final String UPDATE_ACTION = "/updateSubstitution";
    public static final String EDIT_ACTION = "/editSubstitution";
    public static final String RETURN_ACTION = "/manage_executor.do?id=";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        try {
            SubstitutionForm form = (SubstitutionForm) actionForm;
            Substitution substitution;
            if (form.getId() == null || form.getId() == 0) {
                if (form.isTerminator()) {
                    substitution = new TerminatorSubstitution();
                    substitution.setOrgFunction("");
                } else {
                    substitution = new Substitution();
                }
                substitution.setActorId(form.getActorId());
            } else {
                substitution = Delegates.getSubstitutionService().getSubstitution(getLoggedUser(request), form.getId());
            }
            SubstitutionCriteria criteria = null;
            if (form.getCriteriaId() != null && form.getCriteriaId() != 0) {
                criteria = Delegates.getSubstitutionService().getCriteria(getLoggedUser(request), form.getCriteriaId());
            }
            substitution.setCriteria(criteria);
            substitution.setEnabled(form.isEnabled());
            if (!(substitution instanceof TerminatorSubstitution)) {
                substitution.setOrgFunction(form.buildOrgFunction());
            }
            if (form.getId() == null || form.getId() == 0) {
                Delegates.getSubstitutionService().createSubstitution(getLoggedUser(request), substitution);
            } else {
                Delegates.getSubstitutionService().updateSubstitution(getLoggedUser(request), substitution);
            }
            return new ActionForward(RETURN_ACTION + substitution.getActorId(), true);
        } catch (Exception e) {
            addError(request, e);
            return mapping.findForward(Resources.FORWARD_FAILURE);
        }
    }
}
