package ru.runa.af.web.tag;

import java.util.List;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.MessagesExecutor;
import ru.runa.af.web.action.RemoveExecutorsFromGroupAction;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.MessagesCommon;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "listGroupMembersForm")
public class ListGroupMembersFormTag extends ListExecutorsBaseFormTag {

    private static final long serialVersionUID = -2400457393576894819L;

    @Override
    public String getSubmitButtonName() {
        return MessagesCommon.BUTTON_REMOVE.message(pageContext);
    }

    @Override
    protected boolean isVisible() {
        return getExecutor() instanceof Group
                && Delegates.getAuthorizationService().isAllowed(getUser(), Permission.READ, SecuredObjectType.GROUP, getIdentifiableId());
    }

    @Override
    protected List<? extends Executor> getExecutors() {
        ExecutorService executorService = Delegates.getExecutorService();
        return executorService.getGroupChildren(getUser(), (Group) getExecutor(), getBatchPresentation(), false);
    }

    @Override
    protected int getExecutorsCount() {
        ExecutorService executorService = Delegates.getExecutorService();
        // java.lang.ClassCastException: ru.runa.wfe.user.Actor cannot be cast
        // to ru.runa.wfe.user.Group
        // at
        // ru.runa.af.web.tag.ListGroupMembersFormTag.getExecutorsCount(ListGroupMembersFormTag.java:67)
        // at
        // ru.runa.af.web.tag.ListExecutorsBaseFormTag.fillFormData(ListExecutorsBaseFormTag.java:78)
        return executorService.getGroupChildrenCount(getUser(), (Group) getExecutor(), getBatchPresentation(), false);
    }

    @Override
    protected String getTitle() {
        return MessagesExecutor.TITLE_GROUP_MEMBERS.message(pageContext);
    }

    @Override
    public String getAction() {
        return RemoveExecutorsFromGroupAction.ACTION_PATH;
    }

    @Override
    protected Permission getExecutorsPermission() {
        return Permission.LIST;
    }

    @Override
    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.REMOVE_EXECUTORS_FROM_GROUPS_PARAMETER;
    }
}
