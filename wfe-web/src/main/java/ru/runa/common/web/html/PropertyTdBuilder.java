package ru.runa.common.web.html;

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;
import ru.runa.common.web.Resources;
import ru.runa.common.web.html.TdBuilder.Env.SecuredObjectExtractor;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.security.Permission;

public class PropertyTdBuilder extends BaseTdBuilder {
    private final String propertyName;
    private final AuthState authState;

    public PropertyTdBuilder(Permission permission, String propertyName) {
        super(permission);
        this.propertyName = propertyName;
        authState = permission == Permission.NONE ? AuthState.ALWAYS_ENABLE : AuthState.ASK_WFE;
    }

    public PropertyTdBuilder(Permission permission, String propertyName, Boolean isAlwaysDisabled) {
        super(permission);
        this.propertyName = propertyName;
        authState = isAlwaysDisabled ? AuthState.ALWAYS_DISABLE : AuthState.ASK_WFE;
    }

    public PropertyTdBuilder(Permission permission, String propertyName, SecuredObjectExtractor securedObjectExtractor) {
        super(permission, securedObjectExtractor);
        this.propertyName = propertyName;
        authState = permission == Permission.NONE ? AuthState.ALWAYS_ENABLE : AuthState.ASK_WFE;
    }

    @Override
    public TD build(Object object, Env env) {
        ConcreteElement element;
        if (authState == AuthState.ALWAYS_ENABLE || (authState == AuthState.ASK_WFE && isEnabled(object, env))) {
            element = new A(env.getURL(object), getValue(object, env));
            if (object instanceof WfDefinition && env.getConfirmationMessage(((WfDefinition) object).getVersionId()) != null) {
                element.addAttribute("onclick", env.getConfirmationMessage(((WfDefinition) object).getVersionId()));
            }
        } else {
            element = new StringElement(getValue(object, env));
        }
        TD td = new TD();
        td.setClass(Resources.CLASS_LIST_TABLE_TD);
        td.addElement(element);
        return td;
    }

    @Override
    public String getValue(Object object, Env env) {
        try {
            String property = readProperty(object, propertyName, false);
            return property == null ? "" : property;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @Override
    public String[] getSeparatedValues(Object object, Env env) {
        return new String[] { getValue(object, env) };
    }

    @Override
    public int getSeparatedValuesCount(Object object, Env env) {
        return 1;
    }

    enum AuthState {
        ALWAYS_ENABLE,
        ALWAYS_DISABLE,
        ASK_WFE
    }
}
