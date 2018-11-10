package ru.runa.af.web.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.runa.af.web.action.NtlmLoginAction;
import ru.runa.common.WebResources;
import ru.runa.common.web.filter.HTTPFilterBase;

/**
 * This class in conjunction with {@link ru.runa.af.web.action.NtlmLoginAction}
 * provides NTLM support for IE. Created on 10.11.2005 If user-agent name
 * contails MSIE than filter ask him to pass NTLM authentication again.
 * 
 * @web.filter name="ntlmfilter"
 * @web.filter-mapping url-pattern = "/*"
 */
public class NTLMFilter extends HTTPFilterBase {
    private static final String MSIE = "MSIE";
    private static final String USER_AGENT = "User-Agent";
    private static final String METHOD = "POST";

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (WebResources.isNTLMSupported()) {
            String userAgentName = request.getHeader(USER_AGENT);
            boolean isMSIEUserAgentBrowser = userAgentName != null && userAgentName.indexOf(MSIE) > 0;
            boolean isPostRequest = METHOD.equalsIgnoreCase(request.getMethod());
            if (isMSIEUserAgentBrowser && isPostRequest && NtlmLoginAction.getNTLMPasswordAuthentication(request, response) == null) {
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
