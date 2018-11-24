package ru.runa.wfe.web.framework.extra;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.EnumSet;
import javax.servlet.ServletOutputStream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.user.User;
import ru.runa.wfe.web.framework.core.RequestHandler;
import ru.runa.wfe.web.framework.core.RequestMethod;

/**
 * @param <A> Response data object.
 *
 * @author Dmitry Grigoriev (dimgel)
 */
@CommonsLog
public abstract class JsonHandler<Q, A extends JsonHandler.BaseResponse> extends RequestHandler<Q> {

    /**
     * Common data to be sent with all JSON responses.
     */
    @Getter
    public static class BaseResponse {

        @AllArgsConstructor
        @Getter
        static class CurrentUser {
            long id;
            String name;
        }

        private final CurrentUser currentUser;

        protected BaseResponse(User u) {
            currentUser = (u == null) ? null : new CurrentUser(u.getActor().getId(), u.getName());
        }
    }

    /**
     * Helper for subclasses.
     */
    @Getter
    public static class ListResponse<R> extends BaseResponse {
        private final int count;
        private final ArrayList<R> rows;

        protected ListResponse(User u, int count) {
            super(u);
            this.count = count;
            this.rows = new ArrayList<>(count);
        }
    }

    /**
     * Full JSON response contains error message and data returned by executeImpl().
     */
    @Getter
    private static final class ResponseWrapper<A> {
        private final String error;
        private final A data;

        private ResponseWrapper(String error, A data) {
            this.error = error;
            this.data = data;
        }
    }

    public JsonHandler(EnumSet<RequestMethod> acceptMethods, Class<Q> paramsClass) {
        super(acceptMethods, paramsClass);
    }

    @Override
    protected final void execute() throws Exception {
        ResponseWrapper<A> re;
        try {
            //noinspection unchecked
            re = new ResponseWrapper<>(null, executeImpl());
        } catch (Throwable e) {
            log.error(e);
            re = new ResponseWrapper<>(e.getMessage(), null);
        }

        httpServletResponse.setContentType("application/json; charset=UTF-8");
        ServletOutputStream os = httpServletResponse.getOutputStream();
        ObjectMapper m = new ObjectMapper();
        m.writeValue(os, re);
    }

    protected abstract A executeImpl() throws Exception;
}
