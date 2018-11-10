package ru.runa.wfe.audit;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("PIDel")
public class ProcessDeleteLog extends SystemLog {
    private String name;
    private Long processId;

    protected ProcessDeleteLog() {
    }

    public ProcessDeleteLog(Long actorId, String name, Long processId) {
        super(actorId);
        this.name = name;
        this.processId = processId;
    }

    @Column(name = "PROCESS_DEFINITION_NAME", length = 1024)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "PROCESS_ID")
    public Long getProcessId() {
        return processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }
}
