package ru.runa.wfe.graph.image.figure.bpmn;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.graph.image.figure.AbstractFigure;
import ru.runa.wfe.graph.image.figure.AbstractFigureFactory;
import ru.runa.wfe.graph.image.figure.TransitionFigure;
import ru.runa.wfe.lang.Node;

public class BpmnFigureFactory extends AbstractFigureFactory {

    @Override
    public AbstractFigure createFigure(Node node, boolean useEdgingOnly) {
        AbstractFigure figure = null;
        switch (node.getNodeType()) {
        case TASK_STATE:
            figure = new TaskNodeFigure();
            break;
        case MULTI_TASK_STATE:
            figure = new TaskNodeFigure();
            break;
        case EXCLUSIVE_GATEWAY:
            figure = new Rhomb("image/bpmn/decision.png");
            break;
        case PARALLEL_GATEWAY:
            figure = new Rhomb("image/bpmn/fork_join.png");
            break;
        case START_EVENT:
            figure = new Circle("image/bpmn/start.png");
            break;
        case END_PROCESS:
            figure = new Circle("image/bpmn/end.png");
            break;
        case END_TOKEN:
            figure = new Circle("image/bpmn/endtoken.png");
            break;
        case SUBPROCESS:
            figure = new SubprocessRect();
            break;
        case ACTION_NODE:
            figure = new RoundedRect("image/bpmn/script.png");
            break;
        case WAIT_STATE:
        case TIMER:
            figure = new Circle("image/bpmn/timer.png");
            break;
        case MULTI_SUBPROCESS:
            figure = new SubprocessRect();
            break;
        case SEND_MESSAGE:
            figure = new Circle("image/bpmn/sendmessage.png");
            break;
        case RECEIVE_MESSAGE:
            figure = new Circle("image/bpmn/receivemessage.png");
            break;
        case TEXT_ANNOTATION:
            figure = new TextAnnotationFigure();
            break;
        default:
            throw new InternalApplicationException("Unexpected figure type found: " + node.getNodeType());
        }
        figure.initFigure(node, useEdgingOnly);
        return figure;
    }

    @Override
    public TransitionFigure createTransitionFigure() {
        return new BpmnTransitionFigure();
    }
}
