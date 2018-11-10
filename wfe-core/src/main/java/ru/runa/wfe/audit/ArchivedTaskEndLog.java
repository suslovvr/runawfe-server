package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ExecutorNameValue;

@Entity
@DiscriminatorValue(value = "3")
public class ArchivedTaskEndLog extends ArchivedTaskLog implements TaskEndLog {

    @Override
    @Transient
    public Type getType() {
        return Type.TASK_END;
    }

    @Override
    @Transient
    public String getActorName() {
        String actorName = getAttribute(ATTR_ACTOR_NAME);
        if (actorName != null) {
            return actorName;
        }
        return "";
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getTaskName(), new ExecutorNameValue(getActorName()) };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskEndLog(this);
    }
}
