package ru.runa.af.web.action;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.runa.af.web.form.UpdatePermissionsForm;
import ru.runa.common.web.PermissionWebUtils;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.security.ApplicablePermissions;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;

/**
 * @struts:action path="/updatePermissions" name="updatePermissionsForm" validate="false"
 */
public class UpdatePermissionsAction extends ActionBase {

    public static final String ACTION_PATH = "/updatePermissions";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        UpdatePermissionsForm form = (UpdatePermissionsForm) actionForm;
        try {
            SecuredObjectType securedObjectType = SecuredObjectType.valueOf(form.getSecuredObjectType());
            SecuredObject object = Delegates.getAuthorizationService().findSecuredObject(securedObjectType, form.getId());
            List<Long> executorIds = Lists.newArrayList();
            List<Collection<Permission>> executorPermissions = Lists.newArrayList();
            for (Long executorId : form.getIds()) {
                executorIds.add(executorId);
                List<Permission> permissions = Lists.newArrayList();
                for (String name : form.getPermissions(executorId).getPermissionNames()) {
                    Permission p = Permission.valueOf(name);
                    ApplicablePermissions.check(securedObjectType, p);
                    permissions.add(p);
                }
                executorPermissions.add(permissions);
            }
            // unset permissions
            BatchPresentation batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
            List<Executor> executors = Delegates.getAuthorizationService().getExecutorsWithPermission(getLoggedUser(request), object,
                    batchPresentation, true);
            for (Executor executor : executors) {
                if (!executorIds.contains(executor.getId())) {
                    executorIds.add(executor.getId());
                    executorPermissions.add(new ArrayList<>());
                }
            }
            Delegates.getAuthorizationService().setPermissions(getLoggedUser(request), executorIds, executorPermissions, object);
        } catch (Exception e) {
            addError(request, e);
        }

        return PermissionWebUtils.getReturnActionForward(form);
    }
}
