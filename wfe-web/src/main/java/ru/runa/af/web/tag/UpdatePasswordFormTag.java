package ru.runa.af.web.tag;

import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.MessagesExecutor;
import ru.runa.af.web.action.UpdatePasswordAction;
import ru.runa.af.web.html.PasswordTableBuilder;
import ru.runa.common.web.MessagesCommon;
import ru.runa.wfe.user.Actor;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "updatePasswordForm")
public class UpdatePasswordFormTag extends UpdateExecutorBaseFormTag {

    private static final long serialVersionUID = -3273077346043267061L;

    @Override
    public void fillFormData(TD tdFormElement) {
        PasswordTableBuilder builder = new PasswordTableBuilder(!isSubmitButtonEnabled(), pageContext);
        tdFormElement.addElement(builder.build());
    }

    @Override
    public String getSubmitButtonName() {
        return MessagesCommon.BUTTON_APPLY.message(pageContext);
    }

    @Override
    protected boolean isVisible() {
        boolean result = false;
        if ((getExecutor() instanceof Actor) && isSubmitButtonEnabled()) {
            result = true;
        }
        return result;
    }

    @Override
    protected boolean isSubmitButtonEnabled() {
        return super.isSubmitButtonEnabled() || getUser().getActor().equals(getSecuredObject());
    }

    @Override
    protected String getTitle() {
        return MessagesExecutor.TITLE_ACTOR_PASSWORD.message(pageContext);
    }

    @Override
    public String getAction() {
        return UpdatePasswordAction.ACTION_PATH;
    }
}
