package ru.runa.wfe.lang;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import ru.runa.wfe.execution.ExecutionContext;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class GraphElement implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;
    protected static final Log log = LogFactory.getLog("wfelang");

    private String nodeId;
    protected String name;
    protected String description;
    @XmlTransient
    protected ParsedProcessDefinition parsedProcessDefinition;
    @XmlTransient
    protected GraphElement parentElement;
    @XmlTransient
    protected Map<String, ActionEvent> actionEvents = Maps.newHashMap();
    private int[] graphConstraints;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = null == nodeId ? null : nodeId.intern();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = null == name ? null : name.intern();
        if (getNodeId() == null) {
            setNodeId(name);
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = null == description ? null : description.intern();
    }

    public void setParsedProcessDefinition(ParsedProcessDefinition parsedProcessDefinition) {
        this.parsedProcessDefinition = parsedProcessDefinition;
    }

    public ParsedProcessDefinition getParsedProcessDefinition() {
        return parsedProcessDefinition;
    }

    public int[] getGraphConstraints() {
        return graphConstraints;
    }

    public void setGraphConstraints(int x, int y, int width, int height) {
        this.graphConstraints = new int[] { x, y, width, height };
    }

    /**
     * Checks all prerequisites needed for execution.
     */
    public void validate() {
        Preconditions.checkNotNull(nodeId, "id in " + this);
        Preconditions.checkNotNull(name, "name in " + this);
    }

    public Map<String, ActionEvent> getEvents() {
        return actionEvents;
    }

    public ActionEvent getEventNotNull(String eventType) {
        ActionEvent actionEvent = actionEvents.get(eventType);
        if (actionEvent == null) {
            actionEvent = new ActionEvent(eventType);
            addEvent(actionEvent);
        }
        return actionEvent;
    }

    public ActionEvent addEvent(ActionEvent actionEvent) {
        Preconditions.checkArgument(actionEvent != null, "can't add null event to graph element");
        Preconditions.checkArgument(actionEvent.getEventType() != null, "can't add an event without type to graph element");
        actionEvents.put(actionEvent.getEventType(), actionEvent);
        return actionEvent;
    }

    public Action getAction(String id) {
        for (Entry<String, ActionEvent> entry : getEvents().entrySet()) {
            for (Action action : entry.getValue().getActions()) {
                if (id.equals(action.getNodeId())) {
                    return action;
                }
            }
        }
        return null;
    }

    public void fireEvent(ExecutionContext executionContext, String eventType) {
        log.debug("event '" + eventType + "' on '" + this + "' for '" + executionContext.getToken() + "'");
        // execute static actions
        ActionEvent actionEvent = getEventNotNull(eventType);
        // execute the static actions specified in the process definition
        executeActions(executionContext, actionEvent.getActions());
        // propagate the event to the parent element
        GraphElement parentElement = getParentElement();
        if (parentElement != null) {
            parentElement.fireEvent(executionContext, eventType);
        }
    }

    protected void executeActions(ExecutionContext executionContext, List<Action> actions) {
        for (Action action : actions) {
            action.execute(executionContext);
        }
    }

    public GraphElement getParentElement() {
        return parentElement != null ? parentElement : parsedProcessDefinition;
    }

    public void setParentElement(GraphElement parentElement) {
        this.parentElement = parentElement;
    }

    @Override
    public GraphElement clone() throws CloneNotSupportedException {
        GraphElement clone = (GraphElement) super.clone();
        clone.graphConstraints = graphConstraints == null ? null : graphConstraints.clone();
        return clone;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", getNodeId()).add("name", getName()).toString();
    }
}
