package ru.runa.common.web.html;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import javax.servlet.jsp.PageContext;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import ru.runa.af.web.MessagesExecutor;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdsForm;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.security.ApplicablePermissions;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

/**
 * Builds HTML Table of executors with their own permissions on given securedObject.
 */
public class PermissionTableBuilder {
    private final SecuredObject securedObject;
    private final User user;
    private final PageContext pageContext;
    private final List<Permission> applicablePermissions;
    private final boolean updateAllowed;
    private final Map<Executor, List<Permission>> additionalExecutors = Maps.newHashMap();

    public PermissionTableBuilder(SecuredObject securedObject, User user, PageContext pageContext) {
        this.securedObject = securedObject;
        this.user = user;
        this.pageContext = pageContext;
        applicablePermissions = ApplicablePermissions.listVisible(securedObject);
        updateAllowed = Delegates.getAuthorizationService().isAllowed(user, Permission.UPDATE_PERMISSIONS, securedObject);
    }

    public void addAdditionalExecutor(Executor executor, List<Permission> unmodifiablePermissions) {
        additionalExecutors.put(executor, unmodifiablePermissions);
    }

    public Table buildTable() {
        BatchPresentation batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
        List<Executor> executors = Delegates.getAuthorizationService().getExecutorsWithPermission(user, securedObject, batchPresentation, true);
        executors.removeAll(additionalExecutors.keySet());
        Table table = new Table();
        table.setClass(Resources.CLASS_PERMISSION_TABLE);
        table.addElement(createTableHeaderTR());
        List<Permission> noPermissions = Lists.newArrayList();
        for (Executor executor : executors) {
            table.addElement(createTR(executor, noPermissions, false));
        }
        for (Map.Entry<Executor, List<Permission>> entry : additionalExecutors.entrySet()) {
            table.addElement(createTR(entry.getKey(), entry.getValue(), true));
        }
        return table;
    }

    private TR createTableHeaderTR() {
        TR tr = new TR();
        tr.addElement(new TH(HTMLUtils.createSelectionStatusPropagator()).setClass(Resources.CLASS_PERMISSION_TABLE_TH));
        tr.addElement(new TH(MessagesExecutor.EXECUTOR_NAME.message(pageContext)).setClass(Resources.CLASS_PERMISSION_TABLE_TH));
        for (Permission permission : applicablePermissions) {
            String permissioni18nName = Messages.getMessage("permission." + permission.getName(), pageContext);
            tr.addElement(new TH(permissioni18nName).setClass(Resources.CLASS_PERMISSION_TABLE_TH));
        }
        return tr;
    }

    private TR createTR(Executor executor, List<Permission> unmodifiablePermissions, boolean additionalExecutor) {
        List<Permission> ownPermissions = Delegates.getAuthorizationService().getIssuedPermissions(user, executor, securedObject);
        boolean executorIsPrivileged = ownPermissions.isEmpty() && !additionalExecutor;

        TR tr = new TR();

        // Column of "don't remove executor" checkboxes.
        Input input = new Input(Input.CHECKBOX, IdsForm.IDS_INPUT_NAME, String.valueOf(executor.getId()));
        input.setChecked(true);
        input.setDisabled(executorIsPrivileged || additionalExecutor);
        tr.addElement(new TD(input).setClass(Resources.CLASS_PERMISSION_TABLE_TD));

        // Column of executor names.
        tr.addElement(new TD(HTMLUtils.createExecutorElement(pageContext, executor)).setClass(Resources.CLASS_PERMISSION_TABLE_TD));

        // Columns of permissions.
        for (Permission permission : applicablePermissions) {
            String name = "executor(" + executor.getId() + ").permission(" + permission.getName() + ")";
            boolean checked = (!additionalExecutor && ownPermissions.isEmpty()) || ownPermissions.contains(permission);
            Input checkbox = new Input(Input.CHECKBOX, name);
            checkbox.setChecked(checked);
            checkbox.setDisabled(executorIsPrivileged || !updateAllowed || unmodifiablePermissions.contains(permission));
            tr.addElement(new TD(checkbox).setClass(Resources.CLASS_PERMISSION_TABLE_TD));
        }

        if (additionalExecutor) {
            tr.addElement(new Input(Input.HIDDEN, IdsForm.IDS_INPUT_NAME, String.valueOf(executor.getId())));
        }

        return tr;
    }
}
