package ru.runa.af.web.tag;

import org.apache.ecs.html.Form;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.action.DeployDataSourceAction;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.FileForm;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.MessagesDataSource;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "deployDataSource")
public class DeployDataSourceTag extends TitledFormTag {

    private static final long serialVersionUID = 1L;

    @Override
    protected boolean isSubmitButtonEnabled() {
        return Delegates.getExecutorService().isAdministrator(getUser());
    }

    @Override
    protected String getSubmitButtonName() {
        return MessagesDataSource.BUTTON_DEPLOY_DATA_SOURCE.message(pageContext);
    }

    @Override
    protected String getTitle() {
        return MessagesDataSource.BUTTON_DEPLOY_DATA_SOURCE.message(pageContext);
    }

    @Override
    public String getAction() {
        return DeployDataSourceAction.ACTION_PATH;
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        if (!Delegates.getExecutorService().isAdministrator(getUser())) {
            throw new AuthorizationException("No permission on this page");
        }
        getForm().setEncType(Form.ENC_UPLOAD);
        Input fileUploadInput = new Input(Input.FILE, FileForm.FILE_INPUT_NAME);
        fileUploadInput.setClass(Resources.CLASS_REQUIRED);
        tdFormElement.addElement(fileUploadInput);
    }
}
