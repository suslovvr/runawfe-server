package ru.runa.common.web.html;

import java.util.HashMap;
import java.util.Map;

import ru.runa.common.web.Commons;
import ru.runa.common.web.html.TdBuilder.Env;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

public abstract class EnvBaseImpl implements Env {
    private User user;

    @Override
    public User getUser() {
        if (user == null) {
            user = Commons.getUser(getPageContext().getSession());
        }
        return user;
    }

    @Override
    public boolean hasProcessDefinitionPermission(Permission permission, Long processDefinitionVersionId) {
        try {
            Boolean result = processDefPermissionCache.get(processDefinitionVersionId);
            if (result != null) {
                return result;
            }
            WfDefinition definition = Delegates.getDefinitionService().getProcessDefinition(getUser(), processDefinitionVersionId);
            result = Delegates.getAuthorizationService().isAllowed(getUser(), permission, definition);
            processDefPermissionCache.put(processDefinitionVersionId, result);
            return result;
        } catch (AuthorizationException e) {
            processDefPermissionCache.put(processDefinitionVersionId, false);
            return false;
        }
    }

    // TODO This probably can be optimized: it stores processDefinitionVersionId as key, but should store definitionId,
    //      since permissions check applies to ProcessDefinition, not to ProcessDefinitionVersion.
    private final Map<Long, Boolean> processDefPermissionCache = new HashMap<>();
}
