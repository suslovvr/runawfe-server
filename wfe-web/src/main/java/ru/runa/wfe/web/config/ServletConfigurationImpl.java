package ru.runa.wfe.web.config;

import ru.runa.wfe.web.framework.core.RequestParamsParser;
import ru.runa.wfe.web.framework.core.ServletConfiguration;

public class ServletConfigurationImpl extends ServletConfiguration {

    public ServletConfigurationImpl() {
        super(new UriToHandlerMapperImpl(), new RequestParamsParser());
    }
}
