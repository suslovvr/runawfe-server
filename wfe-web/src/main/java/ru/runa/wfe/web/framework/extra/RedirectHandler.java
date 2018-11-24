package ru.runa.wfe.web.framework.extra;

import javax.servlet.http.HttpServletResponse;
import ru.runa.wfe.web.framework.core.RequestHandler;

/**
 * @author Dmitry Grigoriev (dimgel)
 */
public class RedirectHandler extends RequestHandler<Object> {
    private final String url;

    public RedirectHandler(String url) {
        super(acceptGet, Object.class);
        this.url = url;
    }

    @Override
    protected void execute() {
        httpServletResponse.setStatus(HttpServletResponse.SC_FOUND);
        httpServletResponse.setHeader("Location", url);
    }
}
