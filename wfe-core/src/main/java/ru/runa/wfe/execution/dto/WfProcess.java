package ru.runa.wfe.execution.dto;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.security.SecuredObjectBase;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * Created on 02.11.2004
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class WfProcess extends SecuredObjectBase {
    private static final long serialVersionUID = 4862220986262286596L;
    public static final String SELECTED_TRANSITION_KEY = "RUNAWFE_SELECTED_TRANSITION";
    public static final String TRANSIENT_VARIABLES = "RUNAWFE_TRANSIENT_VARIABLES";

    private Long id;
    private String name;
    private Date startDate;
    private Date endDate;
    private int version;
    private boolean archive;

    /**
     * In fact, this is processDefinitionVersionId. But I cannot change structure which is part of the API.
     */
    private Long definitionId;

    private String hierarchyIds;
    // map is not usable in web services
    private final List<WfVariable> variables = Lists.newArrayList();
    private ExecutionStatus executionStatus;
    private String processErrors = "";

    public WfProcess() {
    }

    public WfProcess(Process process) {
        id = process.getId();
        name = process.getDefinitionVersion().getDefinition().getName();
        definitionId = process.getDefinitionVersion().getId();
        version = process.getDefinitionVersion().getVersion().intValue();
        startDate = process.getStartDate();
        endDate = process.getEndDate();
        hierarchyIds = process.getHierarchyIds();
        executionStatus = process.getExecutionStatus();
        archive = process.isArchive();
    }
    
    public WfProcess (Process process, String processErrors) {
        id = process.getId();
        name = process.getDefinitionVersion().getDefinition().getName();
        definitionId = process.getDefinitionVersion().getId();
        version = process.getDefinitionVersion().getVersion().intValue();
        startDate = process.getStartDate();
        endDate = process.getEndDate();
        hierarchyIds = process.getHierarchyIds();
        executionStatus = process.getExecutionStatus();
        this.processErrors = processErrors;
    }
    
    public String getProcessErrors() {
        return processErrors;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public SecuredObjectType getSecuredObjectType() {
        return SecuredObjectType.PROCESS;
    }

    public String getName() {
        return name;
    }

    /**
     * @return true if process is ended.
     */
    public boolean isEnded() {
        return executionStatus == ExecutionStatus.ENDED;
    }

    public Date getEndDate() {
        return endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public int getVersion() {
        return version;
    }

    /**
     * In fact, this is processDefinitionVersionId. But I cannot change structure which is part of the API.
     */
    public Long getDefinitionId() {
        return definitionId;
    }

    public String getHierarchyIds() {
        return hierarchyIds;
    }

    public void addVariable(WfVariable variable) {
        if (variable != null) {
            variables.add(variable);
        }
    }

    public WfVariable getVariable(String name) {
        for (WfVariable variable : variables) {
            if (Objects.equal(name, variable.getDefinition().getName())) {
                return variable;
            }
        }
        return null;
    }

    public Object getVariableValue(String name) {
        WfVariable variable = getVariable(name);
        if (variable != null) {
            return variable.getValue();
        }
        return null;
    }

    public ExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public boolean isArchive() {
        return archive;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WfProcess) {
            return Objects.equal(id, ((WfProcess) obj).id);
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", id).add("name", name).toString();
    }

}
