package ru.runa.wfe.web.framework.extra;

import ru.runa.wfe.web.framework.core.RequestHandler;

/**
 * @author Dmitry Grigoriev (dimgel)
 */
public class JspHandler extends RequestHandler<Object> {
    private final String jspFilePath;

    public JspHandler(String jspFilePath) {
        super(acceptGet, Object.class);
        this.jspFilePath = jspFilePath;
    }

    @Override
    protected void execute() throws Exception {
        httpServletRequest.getRequestDispatcher("/WEB-INF/ui2/" + jspFilePath).forward(httpServletRequest, httpServletResponse);
    }
}
