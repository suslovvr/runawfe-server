package ru.runa.wfe.service.interceptors;

import com.google.common.base.Throwables;
import java.util.Arrays;
import java.util.List;
import javax.ejb.EJBException;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthenticationExpiredException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.service.impl.MessagePostponedException;
import ru.runa.wfe.service.utils.ApiProperties;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.validation.ValidationException;

/**
 * Interceptor for logging and original exception extractor (from
 * {@link EJBException}).
 *
 * @author Dofs
 * @since RunaWFE 4.0
 */
@CommonsLog
public class EjbExceptionSupport {

    public static final List<Class<? extends InternalApplicationException>> warnExceptionClasses = Arrays.asList(
            AuthenticationExpiredException.class, AuthenticationException.class, AuthorizationException.class,
            ExecutorDoesNotExistException.class, ValidationException.class, TaskDoesNotExistException.class
    );

    @AroundInvoke
    public Object process(InvocationContext ic) throws Exception {
        try {
            return ic.proceed();
        } catch (Throwable th) {
            if (th instanceof MessagePostponedException) {
                log.debug(th);
                throw (MessagePostponedException) th;
            }
            if (warnExceptionClasses.contains(th.getClass())) {
                log.warn("ejb call " + th);
            } else {
                log.error("ejb call error: " + DebugUtils.getDebugString(ic, true), th);
            }
            if (ApiProperties.suppressExternalExceptions()) {
                if (th instanceof InternalApplicationException) {
                    throw (InternalApplicationException) th;
                }
                throw new InternalApplicationException(th.getMessage());
            }
            Throwables.propagateIfInstanceOf(th, Exception.class);
            throw Throwables.propagate(th);
        }
    }

}
