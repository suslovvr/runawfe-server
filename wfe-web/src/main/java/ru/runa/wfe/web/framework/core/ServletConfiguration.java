package ru.runa.wfe.web.framework.core;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * Must be subclassed. Subclass must have default constructor.
 * Full subclass name must be specified as {@link Servlet} init parameter "configurationClass" in web.xml.
 *
 * @see Servlet
 * @author Dmitry Grigoriev (dimgel)
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ServletConfiguration {

    final UriToHandlerMapper uriToHandlerMapper;

    /**
     * If you don't have application-specific parameter types or parameter parsing logic, you may specify instance of RequestParamsParser itself.
     */
    final RequestParamsParser requestParamsParser;
}
