package ru.runa.wfe.web.config;

import java.util.HashMap;
import lombok.val;
import ru.runa.wfe.web.api.GetMyTasks;
import ru.runa.wfe.web.api.GetProcessDefs;
import ru.runa.wfe.web.framework.core.RequestHandler;
import ru.runa.wfe.web.framework.core.RequestMethod;
import ru.runa.wfe.web.framework.core.UriToHandlerMapper;
import ru.runa.wfe.web.framework.extra.JspHandler;
import ru.runa.wfe.web.framework.extra.RedirectHandler;

public class UriToHandlerMapperImpl extends UriToHandlerMapper {

    @Override
    protected RequestHandler createHandler(RequestMethod method, String uri, HashMap<String, String> pathParams) throws Exception {
        // Special cases not covered by PathComponents (which does not accept null and is insensitive to trailing slash).
        if (uri.isEmpty() || uri.equals("/ui2")) {
            return new RedirectHandler("/wfe/ui2/");
        }

        val pathComponents = new PathComponents(uri);
        switch (pathComponents.next()) {
            case "api": switch (pathComponents.next()) {
                case "myTasks":
                    return new GetMyTasks();
                case "processDefs":
                    return new GetProcessDefs();
                default:
                    return null;
            }
            case "ui2": switch (pathComponents.next()) {
                case "":
                    // TODO And this is where compiled template engine could allow us to get rid of string literals & dynamic dispatch.
                    return new JspHandler("page/index.jsp");  // SPA
                case "myTasks":
                    return new JspHandler("page/myTasks.jsp");
                case "processDefs":
                    return new JspHandler("page/processDefs.jsp");
                default:
                    return null;
            }
            default:
                return null;
        }
    }
}
