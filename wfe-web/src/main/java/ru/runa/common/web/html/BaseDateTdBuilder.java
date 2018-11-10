package ru.runa.common.web.html;

import java.util.Date;
import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;
import ru.runa.common.web.Commons;
import ru.runa.common.web.form.IdForm;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;

/**
 * Created on 14.11.2005
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
public abstract class BaseDateTdBuilder<T extends SecuredObject> implements TdBuilder {

    @Override
    public TD build(Object object, Env env) {
        TD td;
        Date date = getDate((T) object);
        if (date == null) {
            td = new TD("");
        } else {
            String dateText = getValue(object, env);
            if (isEnabled(env)) {
                String url = Commons.getActionUrl(getActionMapping(), IdForm.ID_INPUT_NAME, String.valueOf(getId((T) object)), env.getPageContext(),
                        PortletUrlType.Resource);
                td = new TD(new A(url, dateText));
            } else {
                td = new TD(dateText);
            }
        }
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        return td;
    }

    @Override
    public String getValue(Object object, Env env) {
        Date date = getDate((T) object);
        if (date != null) {
            return CalendarUtil.formatDateTime(date);
        }
        return "";
    }

    protected abstract Date getDate(T object);

    protected abstract Long getId(T object);

    protected abstract String getActionMapping();

    @Override
    public String[] getSeparatedValues(Object object, Env env) {
        return new String[] { getValue(object, env) };
    }

    @Override
    public int getSeparatedValuesCount(Object object, Env env) {
        return 1;
    }

    protected boolean isEnabled(Env env) {
        // This class and its subclasses are used with DEFINITION and PROCESS object lists, so checking READ permission is good default behaviour.
        return env.isAllowed(Permission.READ, new Env.IdentitySecuredObjectExtractor());
    }
}
