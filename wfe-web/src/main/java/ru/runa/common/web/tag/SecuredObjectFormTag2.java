package ru.runa.common.web.tag;

import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.Attribute;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Has "securedObjectType" and "identifiableId" attributes. Base class for universal tags that apply to multiple object types.
 *
 * @see SecuredObjectFormTag
 */
public abstract class SecuredObjectFormTag2 extends TitledFormTag {
    private static final long serialVersionUID = 1L;

    protected SecuredObjectType securedObjectType;
    protected Long identifiableId;

    @Attribute(required = true)
    public void setSecuredObjectType(String type) {
        this.securedObjectType = SecuredObjectType.valueOf(type);
    }

    @Attribute
    public void setIdentifiableId(Long id) {
        this.identifiableId = id;
    }

    protected final SecuredObject getSecuredObject() {
        return Delegates.getAuthorizationService().findSecuredObject(securedObjectType, identifiableId);
    }

    /**
     * @return {@link Permission} that executor must have to update.
     */
    protected abstract Permission getSubmitPermission();

    @Override
    protected boolean isSubmitButtonEnabled() {
        Permission permission = getSubmitPermission();
        return (permission == null) || isSubmitButtonEnabled(getSecuredObject(), permission);
    }

    protected boolean isSubmitButtonEnabled(SecuredObject securedObject, Permission permission) {
        return Delegates.getAuthorizationService().isAllowed(getUser(), permission, securedObject);
    }

    @Override
    public void fillFormElement(TD tdFormElement) {
        tdFormElement.addElement(new Input(Input.HIDDEN, "securedObjectType", securedObjectType.getName()));
        if (identifiableId != null && identifiableId != 0) {
            tdFormElement.addElement(new Input(Input.HIDDEN, "id", identifiableId.toString()));
        }
    }
}
