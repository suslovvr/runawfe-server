package ru.runa.wfe.commons.ftl;

import java.util.Map;

import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.var.VariableDefinition;

/**
 * Interface allows form components make custom parsing of user input.
 *
 * @author dofs
 * @since 4.2.0
 */
public interface FormComponentSubmissionHandler {

    /**
     * Processing method
     *
     * @param interaction
     *            task form interaction
     * @param variableDefinition
     *            variable definition
     * @param userInput
     *            raw user input
     * @param errors
     *            map containing field errors (messages will be displayed to user)
     *
     * @return parsed values
     * @throws Exception
     *             if any error occurs; message will be displayed to user
     */
    public Map<String, ? extends Object> extractVariables(Interaction interaction, VariableDefinition variableDefinition,
            Map<String, ? extends Object> userInput, Map<String, String> errors) throws Exception;

}
