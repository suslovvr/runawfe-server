package ru.runa.wfe.web.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * @param <Q> Request form parsed by Struts.
 * @param <A> Response data that will be wrapped into {@link ResponseWrapper} instance and serialized to JSON.
 */
@CommonsLog
public abstract class Api<Q extends ActionForm, A> extends Action {

    /**
     * Helper for subclasses.
     */
    static class EmptyRequest extends ActionForm {
    }

    /**
     * Helper for subclasses.
     */
    @Getter
    static class ListResponse<R> {
        private final int count;
        private final ArrayList<R> rows;

        ListResponse(int count) {
            this.count = count;
            this.rows = new ArrayList<>(count);
        }
    }

    @Getter
    private static final class ResponseWrapper<A> {
        private final String error;
        private final A data;

        private ResponseWrapper(String error, A data) {
            this.error = error;
            this.data = data;
        }
    }

    /**
     * Sort of functional style: handler takes params and returns response.
     */
    protected abstract A execute(Q form, HttpServletRequest hrq) throws Exception;

    @Override
    public final ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest hrq, HttpServletResponse hre) throws Exception {
        ResponseWrapper<A> re;
        try {
            //noinspection unchecked
            re = new ResponseWrapper<>(null, execute((Q) form, hrq));
        } catch (Throwable e) {
            log.error("Failed to generate Response", e);
            re = new ResponseWrapper<>(e.getMessage(), null);
        }

        hre.setContentType("application/json; charset=UTF-8");
        ServletOutputStream os = hre.getOutputStream();
        try {
            ObjectMapper m = new ObjectMapper();
            m.writeValue(os, re);
        } catch (Throwable e) {
            log.error("Failed to serialize Response", e);
            // We may have already written something, so there's no use in sending well-formed error response from here.
            // But if you want, prevent Jackson from auto-closing output: https://github.com/msgpack/msgpack-java/issues/233#issuecomment-414521036
        }

        return null;
    }
}
