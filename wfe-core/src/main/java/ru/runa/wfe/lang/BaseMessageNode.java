package ru.runa.wfe.lang;

import com.google.common.base.Objects;
import ru.runa.wfe.lang.bpmn2.MessageEventType;
import ru.runa.wfe.var.VariableMapping;

public abstract class BaseMessageNode extends VariableContainerNode {
    private static final long serialVersionUID = 1L;
    public static final String EVENT_TYPE = "event_type";
    public static final String ERROR_EVENT_PROCESS_ID = "processId";
    public static final String ERROR_EVENT_NODE_ID = "processNodeId";
    public static final String ERROR_EVENT_TOKEN_ID = "tokenId";
    public static final String ERROR_EVENT_MESSAGE = "error_event_message";
    private MessageEventType eventType = MessageEventType.message;

    public MessageEventType getEventType() {
        return eventType;
    }

    public void setEventType(MessageEventType eventType) {
        this.eventType = eventType;
    }

    
    public boolean areSelectorsContainVariable(String variableName) {
        for (VariableMapping mapping : this.getVariableMappings()) {
            if (mapping.isPropertySelector() && Objects.equal(mapping.getMappedName(), "${" + variableName + "}")) {
                return true;
            }
        }
        return false;
    }

}
