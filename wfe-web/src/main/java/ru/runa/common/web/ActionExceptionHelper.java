package ru.runa.common.web;

import com.google.common.base.Throwables;
import java.util.Locale;
import javax.security.auth.login.LoginException;
import javax.servlet.jsp.PageContext;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import ru.runa.wf.web.VariablesFormatException;
import ru.runa.wf.web.action.DataFileNotPresentException;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.LocalizableException;
import ru.runa.wfe.definition.DefinitionAlreadyExistException;
import ru.runa.wfe.definition.DefinitionArchiveFormatException;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.DefinitionFileDoesNotExistException;
import ru.runa.wfe.definition.DefinitionNameMismatchException;
import ru.runa.wfe.definition.InvalidDefinitionException;
import ru.runa.wfe.execution.ParentProcessExistsException;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.presentation.filter.FilterFormatException;
import ru.runa.wfe.relation.RelationAlreadyExistException;
import ru.runa.wfe.relation.RelationDoesNotExistException;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthenticationExpiredException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.WeakPasswordException;
import ru.runa.wfe.ss.SubstitutionDoesNotExistException;
import ru.runa.wfe.task.TaskAlreadyAcceptedException;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.ExecutorAlreadyExistsException;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.ExecutorParticipatesInProcessesException;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.validation.ValidationException;

/**
 * Created 27.05.2005
 */
@CommonsLog
public class ActionExceptionHelper {

    public static void addException(ActionMessages errors, Throwable e, Locale locale) {
        e = Throwables.getRootCause(e);
        errors.add(ActionMessages.GLOBAL_MESSAGE, getActionMessage(e, locale));
        // category set to DEBUG due to logging in EJB layer
        // it's logged anyway due to cause in web layer
        log.debug("action exception", e);
    }

    public static String getErrorMessage(Throwable e, PageContext pageContext) {
        ActionMessage actionMessage = getActionMessage(e, pageContext.getRequest().getLocale());
        return Commons.getMessage(actionMessage.getKey(), pageContext, actionMessage.getValues());
    }

    private static ActionMessage getActionMessage(Throwable e, Locale locale) {
        ActionMessage actionMessage;
        if (e instanceof AuthenticationException || e instanceof LoginException || e instanceof AuthenticationExpiredException) {
            actionMessage = new ActionMessage(MessagesException.EXCEPTION_AUTHENTICATION.getKey());
        } else if (e instanceof AuthorizationException) {
            actionMessage = new ActionMessage(MessagesException.EXCEPTION_AUTHORIZATION.getKey());
        } else if (e instanceof WeakPasswordException) {
            actionMessage = new ActionMessage(MessagesException.EXCEPTION_PASSWORD_IS_WEAK.getKey());
        } else if (e instanceof ExecutorDoesNotExistException) {
            ExecutorDoesNotExistException exception = (ExecutorDoesNotExistException) e;
            if (exception.getExecutorClass().equals(Actor.class)) {
                actionMessage = new ActionMessage(MessagesException.EXCEPTION_ACTOR_DOES_NOT_EXISTS.getKey(), exception.getExecutorName());
            } else if (exception.getExecutorClass().equals(Group.class)) {
                actionMessage = new ActionMessage(MessagesException.EXCEPTION_GROUP_DOES_NOT_EXISTS.getKey(), exception.getExecutorName());
            } else {
                actionMessage = new ActionMessage(MessagesException.EXCEPTION_EXECUTOR_DOES_NOT_EXISTS.getKey(), exception.getExecutorName());
            }
        } else if (e instanceof ExecutorAlreadyExistsException) {
            ExecutorAlreadyExistsException exception = (ExecutorAlreadyExistsException) e;
            actionMessage = new ActionMessage(MessagesException.EXCEPTION_EXECUTOR_ALREADY_EXISTS.getKey(), exception.getExecutorName());
        } else if (e instanceof ExecutorParticipatesInProcessesException) {
            ExecutorParticipatesInProcessesException exception = (ExecutorParticipatesInProcessesException) e;
            actionMessage = new ActionMessage(MessagesException.EXCEPTION_EXECUTOR_PARTICIPATES_IN_PROCESSES.getKey(), exception.getExecutorName(),
                    exception.getIdsInfo());
        } else if (e instanceof ProcessDoesNotExistException) {
            actionMessage = new ActionMessage(MessagesException.ERROR_PROCESS_DOES_NOT_EXIST.getKey(), e.getMessage());
        } else if (e instanceof DefinitionAlreadyExistException) {
            DefinitionAlreadyExistException exception = (DefinitionAlreadyExistException) e;
            actionMessage = new ActionMessage(MessagesException.ERROR_DEFINITION_ALREADY_EXISTS.getKey(), exception.getName());
        } else if (e instanceof DefinitionDoesNotExistException) {
            DefinitionDoesNotExistException exception = (DefinitionDoesNotExistException) e;
            actionMessage = new ActionMessage(MessagesException.ERROR_DEFINITION_DOES_NOT_EXIST.getKey(), exception.getQuotedName());
        } else if (e instanceof DefinitionFileDoesNotExistException) {
            actionMessage = new ActionMessage(MessagesException.DEFINITION_FILE_DOES_NOT_EXIST_ERROR.getKey(), e.getMessage());
        } else if (e instanceof DefinitionArchiveFormatException) {
            actionMessage = new ActionMessage(MessagesException.DEFINITION_ARCHIVE_FORMAT_ERROR.getKey());
        } else if (e instanceof InvalidDefinitionException) {
            actionMessage = new ActionMessage(MessagesException.DEFINITION_FILE_FORMAT_ERROR.getKey(),
                    ((InvalidDefinitionException) e).getDefinitionName(), e.getMessage());
        } else if (e instanceof DefinitionNameMismatchException) {
            DefinitionNameMismatchException exception = (DefinitionNameMismatchException) e;
            actionMessage = new ActionMessage(MessagesException.ERROR_DEFINITION_NAME_MISMATCH.getKey(), exception.getExpectedProcessDefinitionName(),
                    exception.getGivenProcessDefinitionName());
        } else if (e instanceof TaskDoesNotExistException) {
            actionMessage = new ActionMessage(MessagesException.ERROR_TASK_DOES_NOT_EXIST.getKey());
        } else if (e instanceof SubstitutionDoesNotExistException) {
            actionMessage = new ActionMessage(MessagesException.SUBSTITUTION_OUT_OF_DATE.getKey());
        } else if (e instanceof InvalidSessionException) {
            actionMessage = new ActionMessage(MessagesException.EXCEPTION_SESSION_INVALID.getKey());
        } else if (e instanceof FilterFormatException) {
            actionMessage = new ActionMessage(MessagesException.EXCEPTION_TABLE_VIEW_SETUP_FORMAT_INCORRECT.getKey());
        } else if (e instanceof TaskAlreadyAcceptedException) {
            actionMessage = new ActionMessage(MessagesException.TASK_WAS_ALREADY_ACCEPTED.getKey(), e.getMessage());
        } else if (e instanceof ParentProcessExistsException) {
            ParentProcessExistsException exc = (ParentProcessExistsException) e;
            actionMessage = new ActionMessage(MessagesException.PROCESS_HAS_SUPER_PROCESS.getKey(), exc.getDefinitionName(),
                    exc.getParentDefinitionName());
        } else if (e instanceof RelationDoesNotExistException) {
            actionMessage = new ActionMessage(MessagesException.MESSAGE_RELATION_GROUP_DOESNOT_EXISTS.getKey(), e.getMessage());
        } else if (e instanceof RelationAlreadyExistException) {
            actionMessage = new ActionMessage(MessagesException.MESSAGE_RELATION_GROUP_EXISTS.getKey(), e.getMessage());
        } else if (e instanceof VariablesFormatException) {
            actionMessage = new ActionMessage(MessagesException.MESSAGE_VARIABLE_FORMAT_ERROR.getKey(),
                    ((VariablesFormatException) e).getErrorFields());
        } else if (e instanceof DataFileNotPresentException) {
            actionMessage = new ActionMessage(MessagesException.EXCEPTION_DATAFILE_NOT_PRESENT.getKey());
        } else if (e instanceof ValidationException) {
            actionMessage = new ActionMessage(MessagesException.MESSAGE_VALIDATION_ERROR.getKey());
        } else if (e instanceof LocalizableException) {
            actionMessage = new ActionMessage(((LocalizableException) e).getLocalizedMessage(locale), false);
        } else if (e instanceof InternalApplicationException) {
            actionMessage = new ActionMessage(MessagesException.EXCEPTION_UNKNOWN.getKey(), e.getMessage());
        } else {
            String message = e.getMessage();
            if (message == null) {
                message = e.getClass().getName();
            }
            actionMessage = new ActionMessage(MessagesException.EXCEPTION_UNKNOWN.getKey(), message);
        }
        return actionMessage;
    }
}
