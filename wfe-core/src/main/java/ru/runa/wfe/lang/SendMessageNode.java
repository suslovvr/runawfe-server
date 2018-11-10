package ru.runa.wfe.lang;

import java.util.Map;

import javax.jms.ObjectMessage;

import ru.runa.wfe.audit.CurrentSendMessageLog;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.var.MapDelegableVariableProvider;
import ru.runa.wfe.var.dto.Variables;

import com.google.common.collect.Maps;

public class SendMessageNode extends BaseMessageNode {
    private static final long serialVersionUID = 1L;

    private String ttlDuration;

    @Override
    public NodeType getNodeType() {
        return NodeType.SEND_MESSAGE;
    }

    public String getTtlDuration() {
        return ttlDuration;
    }

    public void setTtlDuration(String ttlDuration) {
        this.ttlDuration = ttlDuration;
    }

    @Override
    protected void execute(ExecutionContext executionContext) throws Exception {
        Map<String, Object> variables = Maps.newHashMap();
        variables.put(Variables.CURRENT_PROCESS_ID, executionContext.getProcess().getId());
        // back compatibility
        variables.put("currentInstanceId", executionContext.getProcess().getId());
        variables.put(Variables.CURRENT_PROCESS_DEFINITION_NAME, executionContext.getParsedProcessDefinition().getName());
        variables.put(Variables.CURRENT_NODE_NAME, executionContext.getNode().getName());
        variables.put(Variables.CURRENT_NODE_ID, executionContext.getNode().getNodeId());
        MapDelegableVariableProvider variableProvider = new MapDelegableVariableProvider(variables, executionContext.getVariableProvider());
        long ttl = ExpressionEvaluator.evaluateDuration(executionContext.getVariableProvider(), ttlDuration);
        ObjectMessage message = Utils.sendBpmnMessage(variableMappings, variableProvider, ttl);
        String log = Utils.toString(message, true);
        executionContext.addLog(new CurrentSendMessageLog(this, log));
        leave(executionContext);
    }

}
