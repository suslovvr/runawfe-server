package ru.runa.wfe.web.framework.core;

import java.util.EnumSet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * @param <Q> Request parameters object.
 *
 * @author Dmitry Grigoriev (dimgel)
 */
@RequiredArgsConstructor
public abstract class RequestHandler<Q> {
    protected static final EnumSet<RequestMethod> acceptGet = EnumSet.of(RequestMethod.GET);

    // Filled by constructor.
    final EnumSet<RequestMethod> acceptMethods;
    final Class<Q> paramsClass;

    // Filled by Servlet.
    protected Q params;
    protected RequestMethod requestMethod;
    protected HttpServletRequest httpServletRequest;
    protected HttpServletResponse httpServletResponse;

    /**
     * If throws, Servlet will try to respond with HTTP Error 500 (Internal Server Error).
     */
    protected abstract void execute() throws Exception;
}
