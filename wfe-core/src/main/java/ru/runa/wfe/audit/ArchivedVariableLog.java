package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue(value = "0")
public abstract class ArchivedVariableLog extends ArchivedProcessLog implements VariableLog {

    @Override
    @Transient
    public Type getType() {
        return Type.VARIABLE;
    }

    @Override
    @Transient
    public String getVariableName() {
        return getAttributeNotNull(ATTR_VARIABLE_NAME);
    }

    @Override
    @Transient
    public String getVariableNewValueAttribute() {
        return getAttribute(ATTR_NEW_VALUE);
    }

    @Override
    @Transient
    public boolean isFileValue() {
        return ATTR_VALUE_TRUE.equals(getAttribute(ATTR_IS_FILE_VALUE));
    }

    @Override
    @Transient
    public boolean isExecutorValue() {
        return ATTR_VALUE_TRUE.equals(getAttribute(ATTR_IS_EXECUTOR_VALUE));
    }

    @Override
    @Transient
    public Object getVariableNewValue() {
        return CurrentAndArchiveCommons.variableLog_getVariableNewValue(this);
    }

    @Transient
    public Object getVariableNewValueForPattern() {
        return CurrentAndArchiveCommons.variableLog_getVariableNewValueForPattern(this);
    }
}
