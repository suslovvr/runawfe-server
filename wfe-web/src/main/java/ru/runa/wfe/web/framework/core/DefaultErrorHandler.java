package ru.runa.wfe.web.framework.core;

public class DefaultErrorHandler extends RequestHandler<Object> {
    protected final int status;

    public DefaultErrorHandler(int status) {
        super(null /* unused */, Object.class);
        this.status = status;
    }

    @Override
    protected void execute() throws Exception {
        httpServletResponse.sendError(status);
    }
}
