package ru.runa.common.web.tag;

import java.io.IOException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ecs.ConcreteElement;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.Commons;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.wfe.user.Profile;
import ru.runa.wfe.user.User;

/**
 * Created on 02.09.2004
 */
public abstract class VisibleTag extends TagSupport {
    private static final long serialVersionUID = 1L;
    protected final Log log = LogFactory.getLog(getClass());
    private boolean isVisible = false;

    protected abstract ConcreteElement getEndElement();

    protected abstract ConcreteElement getStartElement();

    protected int doStartTagReturnedValue() {
        return Tag.SKIP_BODY;
    }

    protected int doEndTagReturnedValue() {
        return Tag.EVAL_PAGE;
    }

    /**
     * Returns <code>true</code>(dafault) if tag content should be displayed, or
     * <code>false</code> otherwise.
     */
    protected boolean isVisible() {
        return true;
    }

    @Override
    public int doStartTag() {
        JspWriter writer = null;
        try {
            isVisible = isVisible();
            if (isVisible) {
                writer = pageContext.getOut();
                ConcreteElement element = getStartElement();
                element.output(writer);
            }
        } catch (Throwable th) {
            // DEBUG category set due to logging in EJB layer; stack trace
            // is logged only for Web layer errors.
            log.debug("", th);
            try {
                if (writer != null) {
                    writer.write("<span class=\"error\">" + ActionExceptionHelper.getErrorMessage(th, pageContext) + "</span>");
                }
            } catch (IOException e1) {
                // Do nothing.
            }
        }
        return doStartTagReturnedValue();
    }

    protected User getUser() {
        return Commons.getUser(pageContext.getSession());
    }

    protected Profile getProfile() {
        return ProfileHttpSessionHelper.getProfile(pageContext.getSession());
    }

    @Override
    public int doEndTag() {
        if (isVisible) {
            JspWriter writer = pageContext.getOut();
            try {
                ConcreteElement element = getEndElement();
                element.output(writer);
            } catch (Throwable th) {
                // DEBUG category set due to logging in EJB layer; stack trace
                // is logged only for Web layer errors.
                log.debug("", th);
                try {
                    writer.write("<span class=\"error\">" + ActionExceptionHelper.getErrorMessage(th, pageContext) + "</span>");
                } catch (IOException e) {
                    // Do nothing.
                }
            }
        }
        return doEndTagReturnedValue();
    }

}
