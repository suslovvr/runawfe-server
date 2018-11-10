package ru.runa.wfe.definition.par;

import com.google.common.base.Throwables;
import java.util.List;
import lombok.extern.apachecommons.CommonsLog;
import org.dom4j.Document;
import org.dom4j.Element;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.definition.FileDataProvider;
import ru.runa.wfe.definition.InvalidDefinitionException;
import ru.runa.wfe.lang.Bendpoint;
import ru.runa.wfe.lang.GraphElement;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.lang.ParsedSubprocessDefinition;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.lang.Transition;

@CommonsLog
public class GraphXmlParser implements ProcessArchiveParser {
    private static final String NODE_ELEMENT = "node";
    private static final String TRANSITION_ELEMENT = "transition";
    private static final String BENDPOINT_ELEMENT = "bendpoint";

    @Override
    public boolean isApplicableToEmbeddedSubprocess() {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readFromArchive(ProcessArchive archive, ParsedProcessDefinition parsedProcessDefinition) {
        try {
            String fileName = FileDataProvider.GPD_XML_FILE_NAME;
            if (parsedProcessDefinition instanceof ParsedSubprocessDefinition) {
                fileName = parsedProcessDefinition.getNodeId() + "." + fileName;
            }
            byte[] gpdBytes = parsedProcessDefinition.getFileDataNotNull(fileName);
            Document document = XmlUtils.parseWithoutValidation(gpdBytes);
            Element root = document.getRootElement();
            parsedProcessDefinition.setGraphConstraints(0, 0, Integer.parseInt(root.attributeValue("width")),
                    Integer.parseInt(root.attributeValue("height")));
            int xOffset = Integer.parseInt(root.attributeValue("x", "0"));
            int yOffset = Integer.parseInt(root.attributeValue("y", "0"));
            parsedProcessDefinition.setGraphActionsEnabled(Boolean.parseBoolean(root.attributeValue("showActions", "true")));
            List<Element> nodeElements = root.elements(NODE_ELEMENT);
            for (Element nodeElement : nodeElements) {
                String nodeId = nodeElement.attributeValue("name");
                GraphElement graphElement = parsedProcessDefinition.getGraphElementNotNull(nodeId);
                graphElement.setGraphConstraints(Integer.parseInt(nodeElement.attributeValue("x")) - xOffset,
                        Integer.parseInt(nodeElement.attributeValue("y")) - yOffset, Integer.parseInt(nodeElement.attributeValue("width")),
                        Integer.parseInt(nodeElement.attributeValue("height")));
                Node transitionSource;
                if (graphElement instanceof Node) {
                    boolean minimizedView = Boolean.parseBoolean(nodeElement.attributeValue("minimizedView", "false"));
                    ((Node) graphElement).setGraphMinimizedView(minimizedView);
                    transitionSource = (Node) graphElement;
                } else {
                    if (!(graphElement instanceof SwimlaneDefinition)) {
                        log.warn("Ignored graph element " + graphElement + " in " + parsedProcessDefinition);
                    }
                    continue;
                }
                List<Element> transitionElements = nodeElement.elements(TRANSITION_ELEMENT);
                for (Element transitionElement : transitionElements) {
                    String transitionName = transitionElement.attributeValue("name");
                    Transition transition = transitionSource.getLeavingTransitionNotNull(transitionName);
                    List<Element> bendpointElements = transitionElement.elements(BENDPOINT_ELEMENT);
                    for (Element bendpointElement : bendpointElements) {
                        Bendpoint bendpoint = new Bendpoint(Integer.parseInt(bendpointElement.attributeValue("x")) - xOffset,
                                Integer.parseInt(bendpointElement.attributeValue("y")) - yOffset);
                        transition.getBendpoints().add(bendpoint);
                    }
                }
            }
        } catch (Exception e) {
            Throwables.propagateIfInstanceOf(e, InvalidDefinitionException.class);
            throw new InvalidDefinitionException(parsedProcessDefinition.getName(), e);
        }
    }
}
