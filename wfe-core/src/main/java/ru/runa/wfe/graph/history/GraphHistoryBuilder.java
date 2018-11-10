package ru.runa.wfe.graph.history;

import java.util.List;
import ru.runa.wfe.audit.BaseProcessLog;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.graph.view.NodeGraphElement;
import ru.runa.wfe.history.graph.HistoryGraphBuilder;
import ru.runa.wfe.history.graph.HistoryGraphNode;
import ru.runa.wfe.history.layout.CalculateGraphLayout;
import ru.runa.wfe.history.layout.CalculateGraphLayoutContext;
import ru.runa.wfe.history.layout.CalculateSubTreeBounds;
import ru.runa.wfe.history.layout.NodeLayoutData;
import ru.runa.wfe.history.layout.PushWidthDown;
import ru.runa.wfe.history.layout.TransitionOrderer;
import ru.runa.wfe.history.layout.TransitionOrdererContext;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.user.Executor;

/**
 * History graph building and creating tooltip for elements.
 */
public class GraphHistoryBuilder {

    /**
     * Data, required to build process history graph.
     */
    private final GraphHistoryBuilderData data;

    public GraphHistoryBuilder(List<Executor> executors, Process process, ParsedProcessDefinition parsedProcessDefinition,
            List<? extends BaseProcessLog> fullProcessLogs, String subProcessId) {
        this.data = new GraphHistoryBuilderData(executors, process, parsedProcessDefinition, fullProcessLogs, subProcessId);
    }

    /**
     * Creates graph history as image.
     * 
     * @return Returns image bytes.
     */
    public byte[] createDiagram() throws Exception {
        HistoryGraphNode historyGraph = buildHistoryGraph();
        int height = NodeLayoutData.get(historyGraph).getSubtreeHeight();
        int width = NodeLayoutData.get(historyGraph).getSubtreeWidth();
        historyGraph.processBy(new CreateGraphFigures(), new CreateGraphFiguresContext());
        CreateHistoryGraphImage createImageOperation = new CreateHistoryGraphImage(height, width);
        historyGraph.processBy(createImageOperation, new CreateHistoryGraphImageContext());
        return createImageOperation.getImageBytes();
    }

    /**
     * Creates tooltip presentation elements for graph history.
     * 
     * @return Returns list of tooltips for history graph.
     */
    public List<NodeGraphElement> getElements() throws Exception {
        HistoryGraphNode historyGraph = buildHistoryGraph();
        CreateGraphElementPresentation createPresentationOperation = new CreateGraphElementPresentation(data);
        historyGraph.processBy(createPresentationOperation, new CreateGraphElementPresentationContext());
        return createPresentationOperation.getElements();
    }

    /**
     * Creates and layouts history graph. Now it's ready to draw into image or
     * create tooltip elements.
     * 
     * @return Return created history graph.
     */
    private HistoryGraphNode buildHistoryGraph() {
        HistoryGraphNode historyGraph = HistoryGraphBuilder.buildHistoryGraph(data.getProcessLogs(), data.getProcessInstanceData());
        historyGraph.processBy(new CalculateSubTreeBounds(), null);
        historyGraph.processBy(new PushWidthDown(), -1);
        historyGraph.processBy(new TransitionOrderer(), new TransitionOrdererContext());
        historyGraph.processBy(new CalculateGraphLayout(), new CalculateGraphLayoutContext(NodeLayoutData.get(historyGraph).getSubtreeHeight()));
        return historyGraph;
    }
}
