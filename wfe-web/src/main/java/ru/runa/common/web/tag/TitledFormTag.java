package ru.runa.common.web.tag;

import javax.servlet.jsp.tagext.Tag;

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.Attribute;

import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.Resources;

/**
 * Constructs wrapper HTML table with title row around superclass FormTag.
 */
public abstract class TitledFormTag extends FormTag {
    private static final long serialVersionUID = 1L;

    private String title;

    private String width;

    private String height;

    private String align;

    private String valign;

    @Override
    protected ConcreteElement getStartElement() {
        StringBuilder sb = new StringBuilder();
        Table table = new Table();
        if (id != null) {
            table.setID(id);
        }
        if (width != null) {
            table.setWidth(width);
        }
        if (height != null) {
            table.setHeight(height);
        }
        table.setClass(Resources.CLASS_BOX);
        sb.append(table.createStartTag());
        if (getTitle() != null) {
            TR trh = new TR(((TH) new TH().setClass(Resources.CLASS_BOX_TITLE)).addElement(getTitle()));
            sb.append(trh.toString());
        }
        sb.append(new TR().createStartTag());
        TD td = new TD();
        td.setClass(Resources.CLASS_BOX_BODY);
        if (align != null) {
            td.setAlign(align);
        }
        if (valign != null) {
            td.setVAlign(valign);
        }
        sb.append(td.createStartTag());
        return new StringElement(sb.toString());
    }

    @Override
    protected ConcreteElement getEndElement() {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append(super.getEndElement());
        } catch (Throwable th) {
            // DEBUG category set due to logging in EJB layer; stack trace
            // is logged only for Web layer errors.
            log.debug("", th);
            sb.append("<span class=\"error\">" + ActionExceptionHelper.getErrorMessage(th, pageContext) + "</span>");
        }
        sb.append(new TD().createEndTag());
        sb.append(new TR().createEndTag());
        sb.append(new Table().createEndTag());
        return new StringElement(sb.toString());
    }

    @Override
    protected int doEndTagReturnedValue() {
        return Tag.EVAL_PAGE;
    }

    @Override
    protected int doStartTagReturnedValue() {
        int result;
        if (isVisible()) {
            result = Tag.EVAL_BODY_INCLUDE;
        } else {
            result = Tag.SKIP_BODY;
        }
        return result;
    }

    @Attribute(required = false, rtexprvalue = true)
    public void setAlign(String align) {
        this.align = align;
    }

    @Attribute(required = false, rtexprvalue = true)
    public void setHeight(String height) {
        this.height = height;
    }

    @Attribute(required = false, rtexprvalue = true)
    public void setTitle(String title) {
        this.title = title;
    }

    protected String getTitle() {
        return title;
    }

    @Attribute(required = false, rtexprvalue = true)
    public void setValign(String valign) {
        this.valign = valign;
    }

    @Attribute(required = false, rtexprvalue = true)
    public void setWidth(String width) {
        this.width = width;
    }

    public String getWidth() {
        return width;
    }
}
