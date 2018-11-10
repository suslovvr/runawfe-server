package ru.runa.wfe.script;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.script.common.ScriptOperation;
import ru.runa.wfe.script.common.WorkflowScriptDto;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

/**
 * @author dofs
 * @since 4.0
 */
public class AdminScriptRunner {

    public void runScript(byte[] scriptXml, ScriptExecutionContext context, AdminScriptOperationErrorHandler errorHandler)
            throws AdminScriptException {

        try {
            Unmarshaller unmarshaller = JAXBContext.newInstance(WorkflowScriptDto.class).createUnmarshaller();
            WorkflowScriptDto data = (WorkflowScriptDto) unmarshaller.unmarshal(new ByteArrayInputStream(scriptXml));
            prepareScript(data);
            wrapScriptWithErrorHandler(data, errorHandler);
            data.validateFullAndRegister(context, false);
            for (ScriptOperation operation : data.operations) {
                operation.execute(context);
            }
        } catch (Throwable th) {
            if (th instanceof InvocationTargetException) {
                Throwable target = ((InvocationTargetException) th).getTargetException();
                Throwables.propagateIfInstanceOf(target, AdminScriptException.class);
                throw new AdminScriptException(target);
            }
            Throwables.propagateIfInstanceOf(th, AdminScriptException.class);
            throw new AdminScriptException(th);
        }
    }

    /**
     * Called to prepare/transform script. For example if script executed for
     * some bot station, when all bot station operations may receive new
     * botstation name (according to current botstation).
     *
     * @param data
     *            Deserialized script data.
     */
    protected void prepareScript(WorkflowScriptDto data) {
    }

    /**
     * Wrap every operation to handle exceptions, occured during operation
     * execution/validation.
     *
     * @param data
     * @param errorHandler
     */
    private void wrapScriptWithErrorHandler(WorkflowScriptDto data, final AdminScriptOperationErrorHandler errorHandler) {
        data.operations = Lists.transform(data.operations, new Function<ScriptOperation, ScriptOperation>() {
            @Override
            public ScriptOperation apply(ScriptOperation input) {
                return new ErrorHandlerWrappedScriptOperation(input, errorHandler);
            }
        });
    }
}
