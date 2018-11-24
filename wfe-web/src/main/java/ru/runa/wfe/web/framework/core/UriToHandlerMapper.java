package ru.runa.wfe.web.framework.core;

import java.net.URLDecoder;
import java.util.HashMap;
import org.apache.commons.lang.StringUtils;

/**
 * @see ServletConfiguration
 * @author Dmitry Grigoriev (dimgel)
 */
public abstract class UriToHandlerMapper {

    /**
     * Helper for subclasses.
     */
    protected static class PathComponents {
        private final String data[];
        private int nextIndex = 0;
        private String current;

        /**
         * @param pathInfo Must be non-null.
         */
        public PathComponents(String pathInfo) {
            this.data = StringUtils.split(pathInfo, '/');
        }

        /**
         * Never returns null, because switch(null) on caller side throws NPE.
         * INSENSITIVE to trailing slashes, since returns "" for both missing and empty path component.
         */
        public String next() throws Exception {
            current = (nextIndex < data.length) ? URLDecoder.decode(data[nextIndex++], "UTF-8") : "";
            return current;
        }

        /**
         * Use this to get path parameter value, like this:
         *
         * <pre>
         * switch(pathComponents.next()) {
         *     case "":
         *         return null;  // Send error 404 if parameter is mandatory.
         *     default:
         *         pathParams.put("id", pathComponents.current());
         *         ...  // Switch on next subcomponent, or return handler.
         * }
         * </pre>
         */
        public String current() {
            return current;
        }
    }

    /**
     * ATTENTION! RequestHandler is stateful, so this method must create and return NEW instance on each call.
     *
     * @param uri Not null. Combined values of HttpServletRequest.getServletPath() and getPathInfo(). "/" for root URI.
     * @param pathParams Not null. Output parameter: here implementation must store path parameters it encounters during URI parsing.
     */
    protected abstract RequestHandler createHandler(RequestMethod method, String uri, HashMap<String, String> pathParams) throws Exception;
}
