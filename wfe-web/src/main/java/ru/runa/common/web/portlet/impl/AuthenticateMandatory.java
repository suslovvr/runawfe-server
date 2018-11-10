package ru.runa.common.web.portlet.impl;

import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.runa.common.web.Commons;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.TabHttpSessionHelper;
import ru.runa.common.web.portlet.PortletAuthenticator;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Profile;
import ru.runa.wfe.user.User;

public class AuthenticateMandatory implements PortletAuthenticator {
    protected boolean silent = false;

    @Override
    public boolean authenticate(HttpServletRequest request, HttpServletResponse response, PortletSession session) {
        try {
            User user = Commons.getUser(request.getSession());
            if (session.getAttribute(ProfileHttpSessionHelper.PROFILE_ATTRIBUTE_NAME) == null) {
                Profile profile = Delegates.getProfileService().getProfile(user);
                ProfileHttpSessionHelper.setProfile(profile, session);
                TabHttpSessionHelper.setTabForwardName(request.getRequestURL().toString(), session);
            }
        } catch (Exception e) {
            try {
                User user = Delegates.getAuthenticationService().authenticateByCallerPrincipal();
                Delegates.getSystemService().login(user);
                Profile profile = Delegates.getProfileService().getProfile(user);
                Commons.setUser(user, session);
                ProfileHttpSessionHelper.setProfile(profile, session);
                TabHttpSessionHelper.setTabForwardName(request.getRequestURL().toString(), session);
                Commons.setUser(user, request.getSession());
                ProfileHttpSessionHelper.reloadProfile(request.getSession());
                TabHttpSessionHelper.setTabForwardName(request.getRequestURL().toString(), request.getSession());
            } catch (Exception e2) {
                try {
                    if (!silent) {
                        response.getWriter().println("Auth is required");
                    }
                } catch (Exception e4) {
                }
                return false;
            }
        }
        return true;
    }
}
