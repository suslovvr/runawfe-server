package ru.runa.wfe.lang.jpdl;

import com.google.common.collect.Lists;
import java.util.List;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.lang.BaseMessageNode;
import ru.runa.wfe.lang.BoundaryEvent;
import ru.runa.wfe.lang.BoundaryEventContainer;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.Transition;

public class ReceiveMessageNode extends BaseMessageNode  implements BoundaryEventContainer {
    private static final long serialVersionUID = 1L;
    private final List<BoundaryEvent> boundaryEvents = Lists.newArrayList();

    @Override
    public NodeType getNodeType() {
        return NodeType.RECEIVE_MESSAGE;
    }

    @Override
    protected void execute(ExecutionContext executionContext) throws Exception {
        executionContext.getCurrentToken().setMessageSelector(Utils.getReceiveMessageNodeSelector(executionContext.getVariableProvider(), this));
    }

    @Override
    public void leave(ExecutionContext executionContext, Transition transition) {
        super.leave(executionContext, transition);
        executionContext.getCurrentToken().setMessageSelector(null);
    }

    @Override
    public List<BoundaryEvent> getBoundaryEvents() {
        return boundaryEvents;
    }

}
