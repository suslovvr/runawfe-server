package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ExecutorNameValue;

@Entity
@DiscriminatorValue(value = "V")
public class ArchivedProcessSuspendLog extends ArchivedProcessLog implements ProcessSuspendLog {

    @Override
    @Transient
    public Type getType() {
        return Type.PROCESS_SUSPEND;
    }

    @Override
    @Transient
    public String getActorName() {
        return getAttributeNotNull(ATTR_ACTOR_NAME);
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { new ExecutorNameValue(getActorName()) };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onProcessSuspendLog(this);
    }
}
